package com.example.converterapp.data.local

import androidx.room.*
import com.example.converterapp.domain.model.ConversionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<ConversionHistory>>

    @Insert
    suspend fun insert(history: ConversionHistory)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM history WHERE fromCurrency = :code OR toCurrency = :code")
    fun getHistoryForCurrency(code: String): Flow<List<ConversionHistory>>

    @Query("SELECT * FROM history WHERE date LIKE '%' || :query || '%' OR fromCurrency LIKE '%' || :query || '%' OR toCurrency LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchHistory(query: String): Flow<List<ConversionHistory>>
}