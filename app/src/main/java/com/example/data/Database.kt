package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "journals")
data class Journal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String, // "Ecstatic", "Happy", "Calm", "Anxious", "Sad", "Angry"
    val aiInsight: String? = null,
    val aiAdvice: String? = null,
    val tags: String = "", // Comma-separated list of tags like "Work,Health"
    val isAnalyzing: Boolean = false,
    val analysisError: String? = null
)

@Dao
interface JournalDao {
    @Query("SELECT * FROM journals ORDER BY timestamp DESC")
    fun getAllJournalsFlow(): Flow<List<Journal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal): Long

    @Update
    suspend fun updateJournal(journal: Journal)

    @Query("DELETE FROM journals WHERE id = :id")
    suspend fun deleteJournalById(id: Long)

    @Query("SELECT * FROM journals WHERE id = :id")
    suspend fun getJournalById(id: Long): Journal?
}

@Database(entities = [Journal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_journal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
