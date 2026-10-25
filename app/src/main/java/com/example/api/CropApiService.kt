package com.example.api

import com.example.models.ApiResponse
import com.example.models.Crop
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit Service Interface for the Crop Module.
 * Communicates with the backend PHP REST API endpoint: /api/crops.php
 */
interface CropApiService {

    /**
     * Retrieve all crops or filter by search query and category.
     * Maps to: GET /api/crops.php?search=...&category=...
     */
    @GET("crops.php")
    suspend fun getCrops(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<List<Crop>>>

    /**
     * Retrieve a specific crop by its database primary key.
     * Maps to: GET /api/crops.php?id={id}
     */
    @GET("crops.php")
    suspend fun getCropById(
        @Query("id") id: Int
    ): Response<ApiResponse<Crop>>

    /**
     * Register a new crop entry into the MySQL database.
     * Maps to: POST /api/crops.php
     */
    @POST("crops.php")
    suspend fun addCrop(
        @Body crop: Crop
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Update an existing crop record.
     * Maps to: PUT /api/crops.php
     */
    @PUT("crops.php")
    suspend fun updateCrop(
        @Body crop: Crop
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Delete a crop record by ID.
     * Maps to: DELETE /api/crops.php?id={id}
     */
    @DELETE("crops.php")
    suspend fun deleteCrop(
        @Query("id") id: Int
    ): Response<ApiResponse<Map<String, Any>>>
}
