package com.example.converterapp.data.repository

import com.example.converterapp.data.local.CurrencyDao
import com.example.converterapp.data.remote.CurrencyApiService
import com.example.converterapp.domain.model.Currency
import kotlinx.coroutines.flow.Flow

class CurrencyRepository(
    private val currencyDao: CurrencyDao,
    private val apiService: CurrencyApiService
) {
    fun getAllCurrencies(): Flow<List<Currency>> = currencyDao.getAllCurrencies()

    suspend fun fetchAndSaveCurrenciesFromApi() {
        val response = apiService.getCurrencies()

        val allowedCodes = setOf("USD", "EUR", "CNY")
        val currencies = response.Valute
            .filter { (code, _) -> code in allowedCodes }
            .map { (code, cbr) ->
                Currency(code = code, rateToRub = cbr.Value / cbr.Nominal)
            }
            .toMutableList()

        currencies.add(Currency(code = "RUB", rateToRub = 1.0))

        currencyDao.insertAll(currencies)
    }

    suspend fun updateCurrency(currency: Currency) {
        currencyDao.insert(currency)
    }
    suspend fun deleteCurrency(code: String) = currencyDao.deleteByCode(code)
}