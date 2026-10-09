package com.example.calculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val numericResult: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val angleMode: String = "DEG",
    val isFavorite: Boolean = false
)
