package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class JournalRepository(private val journalDao: JournalDao) {

    val allJournals: Flow<List<Journal>> = journalDao.getAllJournalsFlow()

    suspend fun insert(journal: Journal): Long = withContext(Dispatchers.IO) {
        journalDao.insertJournal(journal)
    }

    suspend fun update(journal: Journal) = withContext(Dispatchers.IO) {
        journalDao.updateJournal(journal)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        journalDao.deleteJournalById(id)
    }

    suspend fun getById(id: Long): Journal? = withContext(Dispatchers.IO) {
        journalDao.getJournalById(id)
    }

    suspend fun analyzeJournal(id: Long) = withContext(Dispatchers.IO) {
        val journal = journalDao.getJournalById(id) ?: return@withContext
        
        // Mark as analyzing
        journalDao.updateJournal(journal.copy(isAnalyzing = true, analysisError = null))

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            journalDao.updateJournal(
                journal.copy(
                    isAnalyzing = false,
                    analysisError = "API Key not configured. Please add GEMINI_API_KEY to your Secrets panel."
                )
            )
            return@withContext
        }

        val promptText = """
            Mood: ${journal.mood}
            Title: ${journal.title}
            Content: ${journal.content}
        """.trimIndent()

        val systemInstructionText = """
            You are an empathetic, insightful mental health and mindfulness journal companion. You analyze the user's daily journal entries and provide:
            1. 'insight': A warm, caring, personalized observation about what they wrote, highlighting positive thoughts or holding space for negative feelings.
            2. 'advice': A supportive, practical, single-sentence tip or exercise they can do to improve or maintain their mental well-being today.
            3. 'tags': A list of up to 3 relevant, highly descriptive category tags (e.g., 'Work', 'Family', 'Self-Care', 'Gratitude', 'Health', 'Stress').

            Your response MUST be a single, valid JSON object with the exact keys: 'insight', 'advice', and 'tags'. Do NOT include any other text outside the JSON block.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptText)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (responseText != null) {
                val result = RetrofitClient.parseAnalysisResult(responseText)
                if (result != null) {
                    journalDao.updateJournal(
                        journal.copy(
                            isAnalyzing = false,
                            aiInsight = result.insight,
                            aiAdvice = result.advice,
                            tags = result.tags.joinToString(","),
                            analysisError = null
                        )
                    )
                } else {
                    journalDao.updateJournal(
                        journal.copy(
                            isAnalyzing = false,
                            analysisError = "Failed to parse analysis from AI. Raw response: ${responseText.take(100)}"
                        )
                    )
                }
            } else {
                journalDao.updateJournal(
                    journal.copy(
                        isAnalyzing = false,
                        analysisError = "No response content received from Gemini AI."
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            journalDao.updateJournal(
                journal.copy(
                    isAnalyzing = false,
                    analysisError = "Network error: ${e.localizedMessage ?: e.message}"
                )
            )
        }
    }
}
