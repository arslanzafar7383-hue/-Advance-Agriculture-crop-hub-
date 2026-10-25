package com.example.api

import com.example.models.ApiResponse
import com.example.models.LoginRequest
import com.example.models.RegisterRequest
import com.example.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Dedicated Retrofit interface for User Authentication & Security validation.
 * Connects to PHP endpoints:
 * - /api/login.php
 * - /api/register.php
 * - /api/test_connection.php
 */
interface AuthApiService {

    @POST("login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<User>>

    @POST("register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<User>>

    @GET("test_connection.php")
    suspend fun testConnection(): Response<ApiResponse<Map<String, Any>>>
}
