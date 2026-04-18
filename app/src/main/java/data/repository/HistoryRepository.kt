package com.example.converterapp.data.repository

import com.example.converterapp.data.local.HistoryDao
import com.example.converterapp.domain.model.ConversionHistory
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    fun getAllHistory(): Flow<List<ConversionHistory>> = historyDao.getAllHistory()
    fun searchHistory(query: String): Flow<List<ConversionHistory>> = historyDao.searchHistory(query)
    suspend fun saveConversion(history: ConversionHistory) = historyDao.insert(history)
    fun getHistoryForCurrency(code: String): Flow<List<ConversionHistory>> = historyDao.getHistoryForCurrency(code)
    suspend fun clearAllHistory() = historyDao.clearAllHistory()
}