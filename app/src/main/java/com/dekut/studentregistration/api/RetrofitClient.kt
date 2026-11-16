package com.dekut.studentregistration.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Wi-Fi Local IP for physical device: 172.16.201.223
    // Emulator IP: 10.0.2.2
    private const val BASE_URL = "http://172.16.201.223:3000/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Interceptor to add ngrok and localtunnel bypass headers
    private val bypassInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("ngrok-skip-browser-warning", "true")
            .addHeader("Bypass-Tunnel-Reminder", "true")
            .addHeader("bypass-tunnel-reminder", "1")
            .addHeader("User-Agent", "DeKUT-Android-App")
            .build()
        chain.proceed(request)
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(bypassInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val mpesaApi: MpesaApi = retrofit.create(MpesaApi::class.java)
}
