package com.example.converterapp.data.local

import androidx.room.*
import com.example.converterapp.domain.model.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies")
    fun getAllCurrencies(): Flow<List<Currency>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<Currency>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currency: Currency)

    @Query("DELETE FROM currencies WHERE code = :code")
    suspend fun deleteByCode(code: String)
}