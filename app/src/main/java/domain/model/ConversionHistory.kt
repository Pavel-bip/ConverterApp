package com.example.converterapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "history")
data class ConversionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Date = Date(),
    val fromCurrency: String,
    val toCurrency: String,
    val amount: Double,
    val result: Double,
    val rate: Double,
    val note: String? = null
)