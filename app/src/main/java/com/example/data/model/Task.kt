package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = "Moyenne", // "Basse", "Moyenne", "Haute"
    val category: String = "Général", // "Général", "Bien-être", "Études", "Santé",
    val description: String = "",
    val customBgColorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
