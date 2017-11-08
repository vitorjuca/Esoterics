package com.br.esoterics.esoadmin.network


import com.br.esoterics.esoadmin.visualmode.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


/**
 * Created by vaniajuca on 06/11/17.
 */

object ApiClient {

    fun apiService(): ApiService {
        return Retrofit.Builder()
                .baseUrl(Constants.DOMAIN)
                .client(httpClient())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
    }

    private fun httpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(BODY))
                .build()
    }

}
