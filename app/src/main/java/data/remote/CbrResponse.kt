package com.example.converterapp.data.remote

data class CbrResponse(
    val Valute: Map<String, CbrCurrency>
)

data class CbrCurrency(
    val Value: Double,
    val Nominal: Int
)