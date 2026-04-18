package com.example.converterapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency(
    @PrimaryKey
    val code: String,
    var rateToRub: Double,
    var updatedDate: Long = System.currentTimeMillis()
)