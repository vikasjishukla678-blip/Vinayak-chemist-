package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Journal
import com.example.ui.theme.CosmicCardBg
import com.example.ui.theme.CosmicCardBorder
import com.example.ui.theme.CosmicDarkBg
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicAccent
import com.example.ui.theme.MoodAngry
import com.example.ui.theme.MoodAnxious
import com.example.ui.theme.MoodCalm
import com.example.ui.theme.MoodEcstatic
import com.example.ui.theme.MoodHappy
import com.example.ui.theme.MoodSad

// Mood Config helper
data class MoodConfig(
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String
)

val moodsList = listOf(
    MoodConfig("Ecstatic", "✨", MoodEcstatic, "Incredible, full of positive energy"),
    MoodConfig("Happy", "☀️", MoodHappy, "Content, joy, and optimistic"),
    MoodConfig("Calm", "🌊", MoodCalm, "Peaceful, grounded, relaxed"),
    MoodConfig("Anxious", "🌀", MoodAnxious, "Overwhelmed, uneasy, racing thoughts"),
    MoodConfig("Sad", "🌧️", MoodSad, "Low energy, reflective, heavy-hearted"),
    MoodConfig("Angry", "🔥", MoodAngry, "Frustrated, irritated, passionate")
)

fun getMoodConfig(moodName: String): MoodConfig {
    return moodsList.find { it.name.equals(moodName, ignoreCase = true) }
        ?: MoodConfig("Calm", "🌊", MoodCalm, "Peaceful")
}

fun getRelativeTimeString(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    val journals by viewModel.filteredJournals.collectAsState()
    val rawJournals by viewModel.journalsState.collectAsState()
    val moodAnalytics by viewModel.moodAnalytics.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMoodFilter by viewModel.selectedMoodFilter.collectAsState()
    val selectedTagFilter by viewModel.selectedTagFilter.collectAsState()

    val newTitle by viewModel.newTitle.collectAsState()
    val newContent by viewModel.newContent.collectAsState()
    val newMood by viewModel.newMood.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var showAnalytics by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CosmicDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Premium ambient radial glow background
                    drawRect(color = CosmicDarkBg)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CosmicPrimary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width,
                        center = Offset(size.width / 2, size.height / 3)
                    )
                }
        ) {
            // 1. App Header & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI Journal",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Empathetic daily reflection with Gemini",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Analytics toggle button (Touch target 48dp)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                    IconButton(
                        onClick = { showAnalytics = !showAnalytics },
                        modifier = Modifier
                            .background(
                                color = if (showAnalytics) CosmicPrimary.copy(alpha = 0.2f) else CosmicCardBg,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (showAnalytics) CosmicPrimary else CosmicCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("toggle_analytics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Mood Analytics Dashboard",
                            tint = if (showAnalytics) CosmicSecondary else Color.White
                        )
                    }
                }
            }

            // 2. Animated Mood Analytics Dashboard
            AnimatedVisibility(
                visible = showAnalytics,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                MoodAnalyticsPanel(
                    moodAnalytics = moodAnalytics,
                    totalJournals = rawJournals.size
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 3. New Journal Entry Form Card
                item {
                    NewJournalForm(
                        newTitle = newTitle,
                        newContent = newContent,
                        newMood = newMood,
                        isSubmitting = isSubmitting,
                        onTitleChange = { viewModel.newTitle.value = it },
                        onContentChange = { viewModel.newContent.value = it },
                        onMoodChange = { viewModel.newMood.value = it },
                        onSave = { viewModel.saveJournal() }
                    )
                }

                // 4. Search and Filters Row
                item {
                    SearchAndFiltersSection(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        selectedMoodFilter = selectedMoodFilter,
                        onMoodFilterChange = { viewModel.setMoodFilter(it) },
                        selectedTagFilter = selectedTagFilter,
                        onTagFilterChange = { viewModel.setTagFilter(it) },
                        allTags = allTags,
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                // 5. Timeline of journals
                if (journals.isEmpty()) {
                    item {
                        EmptyStateView(
                            isFiltered = searchQuery.isNotEmpty() || selectedMoodFilter != null || selectedTagFilter != null,
                            onClearFilters = { viewModel.clearFilters() }
                        )
                    }
                } else {
                    items(journals, key = { it.id }) { journal ->
                        JournalCard(
                            journal = journal,
                            onDelete = { viewModel.deleteJournal(journal.id) },
                            onRetryAnalysis = { viewModel.retryAnalysis(journal.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodAnalyticsPanel(
    moodAnalytics: Map<String, Float>,
    totalJournals: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderDefaults.glowBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Reflections & Mood Balance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "$totalJournals entries total",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalJournals == 0) {
                Text(
                    text = "No journal entries found to calculate metrics. Write your first reflection below to unlock AI insights!",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            } else {
                // Stacked Bar Distribution using Canvas!
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    var currentStartOffset = 0f
                    moodsList.forEach { mood ->
                        val ratio = moodAnalytics[mood.name] ?: 0f
                        if (ratio > 0f) {
                            val width = size.width * ratio
                            drawRect(
                                color = mood.color,
                                topLeft = Offset(currentStartOffset, 0f),
                                size = Size(width, size.height)
                            )
                            currentStartOffset += width
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Legendary Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodsList.chunked(3).forEach { rowMoods ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowMoods.forEach { mood ->
                                val ratio = moodAnalytics[mood.name] ?: 0f
                                val percentage = (ratio * 100).toInt()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(mood.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${mood.emoji} ${mood.name}: $percentage%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (ratio > 0f) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewJournalForm(
    newTitle: String,
    newContent: String,
    newMood: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onMoodChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderDefaults.glowBorder()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "New Cosmic Reflection",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = "Capture your headspace and let Gemini bring clarity",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Mood Selector Row (48dp Touch Target)
            Text(
                text = "Current Headspace",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(moodsList) { mood ->
                    val isSelected = mood.name.equals(newMood, ignoreCase = true)
                    val glowColor by animateColorAsState(
                        targetValue = if (isSelected) mood.color else Color.Transparent,
                        label = "glow"
                    )

                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                        Surface(
                            onClick = { onMoodChange(mood.name) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) mood.color else CosmicCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("mood_selector_${mood.name.lowercase()}"),
                            color = if (isSelected) mood.color.copy(alpha = 0.15f) else CosmicCardBg,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mood.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mood.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Title input
            OutlinedTextField(
                value = newTitle,
                onValueChange = onTitleChange,
                label = { Text("Reflection Title (optional)", color = Color(0xFF64748B)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CosmicPrimary,
                    unfocusedBorderColor = CosmicCardBorder,
                    focusedLabelColor = CosmicPrimary,
                    cursorColor = CosmicPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Content input
            OutlinedTextField(
                value = newContent,
                onValueChange = onContentChange,
                label = { Text("What is on your mind?", color = Color(0xFF64748B)) },
                placeholder = { Text("Be as raw and expressive as you like...", color = Color(0xFF475569)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .padding(bottom = 16.dp)
                    .testTag("content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CosmicPrimary,
                    unfocusedBorderColor = CosmicCardBorder,
                    focusedLabelColor = CosmicPrimary,
                    cursorColor = CosmicPrimary
                ),
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            // Submit Button (Touch target 48dp)
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                Button(
                    onClick = onSave,
                    enabled = newContent.isNotBlank() && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPrimary,
                        disabledContainerColor = CosmicCardBorder
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Engrave Reflection",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFiltersSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedMoodFilter: String?,
    onMoodFilterChange: (String?) -> Unit,
    selectedTagFilter: String?,
    onTagFilterChange: (String?) -> Unit,
    allTags: Set<String>,
    onClearFilters: () -> Unit
) {
    val isAnyFilterActive = searchQuery.isNotEmpty() || selectedMoodFilter != null || selectedTagFilter != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search your thoughts, insights, tags...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Search", tint = Color.White)
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CosmicSecondary,
                unfocusedBorderColor = CosmicCardBorder,
                cursorColor = CosmicSecondary
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mood Filter Chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Filter reflections:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )

            if (isAnyFilterActive) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Clear All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicAccent,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("clear_filters_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal list of Mood Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                    Surface(
                        onClick = { onMoodFilterChange(null) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = if (selectedMoodFilter == null) CosmicSecondary else CosmicCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("filter_mood_all"),
                        color = if (selectedMoodFilter == null) CosmicSecondary.copy(alpha = 0.15f) else CosmicCardBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "All Moods",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedMoodFilter == null) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            items(moodsList) { mood ->
                val isSelected = selectedMoodFilter.equals(mood.name, ignoreCase = true)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                    Surface(
                        onClick = { onMoodFilterChange(mood.name) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) mood.color else CosmicCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("filter_mood_${mood.name.lowercase()}"),
                        color = if (isSelected) mood.color.copy(alpha = 0.15f) else CosmicCardBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = mood.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mood.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Tag Filters
        if (allTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allTags.forEach { tag ->
                    val isSelected = selectedTagFilter == tag
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                        Surface(
                            onClick = { onTagFilterChange(if (isSelected) null else tag) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CosmicPrimary else CosmicCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .testTag("filter_tag_$tag"),
                            color = if (isSelected) CosmicPrimary.copy(alpha = 0.15f) else CosmicCardBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isFiltered: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFiltered) Icons.Default.Search else Icons.Default.Psychology,
            contentDescription = null,
            tint = Color(0xFF475569),
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (isFiltered) "No matches found" else "Slate is Clear",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isFiltered) "Try clearing your filters or search terms to see other entries." 
                   else "Engrave your thoughts, secrets, or daily events above. Gemini will give warm, contemplative insights.",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (isFiltered) {
            Spacer(modifier = Modifier.height(14.dp))
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All Filters", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun JournalCard(
    journal: Journal,
    onDelete: () -> Unit,
    onRetryAnalysis: () -> Unit
) {
    val moodConf = getMoodConfig(journal.mood)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("journal_card_${journal.id}"),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderDefaults.glowBorder(glowColor = moodConf.color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Header: Title, Mood chip, Timestamp and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = journal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Mood pill
                Surface(
                    color = moodConf.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, moodConf.color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = moodConf.emoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = moodConf.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = moodConf.color
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Button (Touch target 48dp)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_button_${journal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Reflection",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = getRelativeTimeString(journal.timestamp),
                fontSize = 11.sp,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Content text
            Text(
                text = journal.content,
                fontSize = 14.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic AI response area
            when {
                journal.isAnalyzing -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkBg, RoundedCornerShape(12.dp))
                            .border(1.dp, CosmicCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = CosmicSecondary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Contemplating your thoughts...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CosmicSecondary
                        )
                    }
                }
                journal.analysisError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x11FF5252), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x33FF5252), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MoodAngry,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Analysis paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoodAngry
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = journal.analysisError,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
                            Button(
                                onClick = onRetryAnalysis,
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicCardBorder),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("retry_button_${journal.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry Connection", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                journal.aiInsight != null -> {
                    // Empathetic insights layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        CosmicPrimary.copy(alpha = 0.08f),
                                        CosmicSecondary.copy(alpha = 0.02f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, CosmicPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CosmicSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini Insight",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = journal.aiInsight,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        if (journal.aiAdvice != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MoodEcstatic,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = journal.aiAdvice,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Tags row
                        if (journal.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                journal.tags.split(",").forEach { tag ->
                                    val cleanTag = tag.trim()
                                    if (cleanTag.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    CosmicDarkBg,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    CosmicCardBorder,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "#$cleanTag",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CosmicSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// BorderStroke helper
@Composable
fun BorderStroke(width: Dp, color: Color) = remember(width, color) {
    androidx.compose.foundation.BorderStroke(width, color)
}

// Custom glow border and defaults
object BorderDefaults {
    @Composable
    fun glowBorder(
        width: Dp = 1.dp,
        glowColor: Color = CosmicCardBorder
    ): androidx.compose.foundation.BorderStroke {
        return BorderStroke(width, glowColor)
    }
}
