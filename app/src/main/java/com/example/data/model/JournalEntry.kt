package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val moodEmoji: String, // e.g. "💖", "😊", "😐", "😔", "😢"
    val moodScore: Int,    // 1 to 5 (1 = very bad, 5 = very good)
    val timestamp: Long = System.currentTimeMillis()
)
