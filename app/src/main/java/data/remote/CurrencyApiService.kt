package com.example.converterapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CurrencyApiService {
    @GET("daily_json.js")
    suspend fun getCurrencies(): CbrResponse

    companion object {
        private const val BASE_URL = "https://www.cbr-xml-daily.ru/"
        fun create(): CurrencyApiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }
}