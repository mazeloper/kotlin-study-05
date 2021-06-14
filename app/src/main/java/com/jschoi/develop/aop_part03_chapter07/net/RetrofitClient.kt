package com.jschoi.develop.aop_part03_chapter07.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var instance: Retrofit? = null

    const val BASE_URL = "https://run.mocky.io"

    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        }
        return instance!!
    }
}