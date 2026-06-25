package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Journal
import com.example.data.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    application: Application,
    private val repository: JournalRepository
) : AndroidViewModel(application) {

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter = MutableStateFlow<String?>(null)

    // Form states
    val newTitle = MutableStateFlow("")
    val newContent = MutableStateFlow("")
    val newMood = MutableStateFlow("Calm") // Default mood

    val isSubmitting = MutableStateFlow(false)

    // Main journals flow
    val journalsState: StateFlow<List<Journal>> = repository.allJournals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered journals flow for the timeline
    val filteredJournals: StateFlow<List<Journal>> = combine(
        journalsState,
        searchQuery,
        selectedMoodFilter,
        selectedTagFilter
    ) { journals, query, mood, tag ->
        journals.filter { journal ->
            val matchesQuery = query.isEmpty() || 
                    journal.title.contains(query, ignoreCase = true) || 
                    journal.content.contains(query, ignoreCase = true) ||
                    journal.tags.contains(query, ignoreCase = true)
            
            val matchesMood = mood == null || journal.mood.equals(mood, ignoreCase = true)
            
            val matchesTag = tag == null || journal.tags.split(",").map { it.trim() }.contains(tag)

            matchesQuery && matchesMood && matchesTag
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Mood analytics calculation
    val moodAnalytics: StateFlow<Map<String, Float>> = journalsState
        .combine(MutableStateFlow(Unit)) { journals, _ ->
            if (journals.isEmpty()) return@combine emptyMap()
            val total = journals.size.toFloat()
            journals.groupBy { it.mood }
                .mapValues { (_, list) -> list.size / total }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // All unique tags across all journals for the filter chips
    val allTags: StateFlow<Set<String>> = journalsState
        .combine(MutableStateFlow(Unit)) { journals, _ ->
            journals.flatMap { journal ->
                journal.tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }.toSet()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun setMoodFilter(mood: String?) {
        selectedMoodFilter.value = mood
    }

    fun setTagFilter(tag: String?) {
        selectedTagFilter.value = tag
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedMoodFilter.value = null
        selectedTagFilter.value = null
    }

    fun saveJournal() {
        val title = newTitle.value.trim()
        val content = newContent.value.trim()
        val mood = newMood.value

        if (content.isEmpty()) return

        val journalTitle = if (title.isEmpty()) "Untitled Reflection" else title

        viewModelScope.launch {
            isSubmitting.value = true
            val newJournal = Journal(
                title = journalTitle,
                content = content,
                mood = mood,
                timestamp = System.currentTimeMillis()
            )
            val insertedId = repository.insert(newJournal)
            
            // Clear input fields
            newTitle.value = ""
            newContent.value = ""
            newMood.value = "Calm"
            isSubmitting.value = false

            // Trigger async Gemini analysis
            repository.analyzeJournal(insertedId)
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun retryAnalysis(id: Long) {
        viewModelScope.launch {
            repository.analyzeJournal(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val repository = JournalRepository(database.journalDao())
                return JournalViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
