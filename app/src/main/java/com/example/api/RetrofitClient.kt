package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var currentBaseUrl: String = ""
    private var apiServiceInstance: CropHubApiService? = null
    private var cropApiServiceInstance: CropApiService? = null
    private var authApiServiceInstance: AuthApiService? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    fun getRetrofit(baseUrl: String): Retrofit {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun getService(baseUrl: String): CropHubApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (apiServiceInstance == null || currentBaseUrl != normalized) {
            currentBaseUrl = normalized
            apiServiceInstance = getRetrofit(normalized).create(CropHubApiService::class.java)
            cropApiServiceInstance = getRetrofit(normalized).create(CropApiService::class.java)
            authApiServiceInstance = getRetrofit(normalized).create(AuthApiService::class.java)
        }
        return apiServiceInstance!!
    }

    fun getCropService(baseUrl: String): CropApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (cropApiServiceInstance == null || currentBaseUrl != normalized) {
            currentBaseUrl = normalized
            apiServiceInstance = getRetrofit(normalized).create(CropHubApiService::class.java)
            cropApiServiceInstance = getRetrofit(normalized).create(CropApiService::class.java)
            authApiServiceInstance = getRetrofit(normalized).create(AuthApiService::class.java)
        }
        return cropApiServiceInstance!!
    }

    fun getAuthService(baseUrl: String): AuthApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (authApiServiceInstance == null || currentBaseUrl != normalized) {
            currentBaseUrl = normalized
            apiServiceInstance = getRetrofit(normalized).create(CropHubApiService::class.java)
            cropApiServiceInstance = getRetrofit(normalized).create(CropApiService::class.java)
            authApiServiceInstance = getRetrofit(normalized).create(AuthApiService::class.java)
        }
        return authApiServiceInstance!!
    }
}
