package com.example.api

import com.example.models.*
import retrofit2.Response
import retrofit2.http.*

interface CropHubApiService {

    @GET("test_connection.php")
    suspend fun testConnection(): Response<ApiResponse<Map<String, Any>>>

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<User>>

    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>

    // CROPS
    @GET("crops.php")
    suspend fun getCrops(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<List<Crop>>>

    @POST("crops.php")
    suspend fun addCrop(@Body crop: Crop): Response<ApiResponse<Map<String, Any>>>

    @PUT("crops.php")
    suspend fun updateCrop(@Body crop: Crop): Response<ApiResponse<Map<String, Any>>>

    @DELETE("crops.php")
    suspend fun deleteCrop(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // FARMERS
    @GET("farmers.php")
    suspend fun getFarmers(@Query("search") search: String? = null): Response<ApiResponse<List<Farmer>>>

    @POST("farmers.php")
    suspend fun addFarmer(@Body farmer: Farmer): Response<ApiResponse<Map<String, Any>>>

    @PUT("farmers.php")
    suspend fun updateFarmer(@Body farmer: Farmer): Response<ApiResponse<Map<String, Any>>>

    @DELETE("farmers.php")
    suspend fun deleteFarmer(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // FIELDS
    @GET("fields.php")
    suspend fun getFields(): Response<ApiResponse<List<FieldEntity>>>

    @POST("fields.php")
    suspend fun addField(@Body field: FieldEntity): Response<ApiResponse<Map<String, Any>>>

    @PUT("fields.php")
    suspend fun updateField(@Body field: FieldEntity): Response<ApiResponse<Map<String, Any>>>

    @DELETE("fields.php")
    suspend fun deleteField(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // PRODUCTION
    @GET("production.php")
    suspend fun getProductions(): Response<ApiResponse<List<CropProduction>>>

    @POST("production.php")
    suspend fun addProduction(@Body record: CropProduction): Response<ApiResponse<Map<String, Any>>>

    @DELETE("production.php")
    suspend fun deleteProduction(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // FERTILIZERS
    @GET("fertilizers.php")
    suspend fun getFertilizers(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<Fertilizer>>>

    @POST("fertilizers.php")
    suspend fun addFertilizer(@Body fertilizer: Fertilizer): Response<ApiResponse<Map<String, Any>>>

    @DELETE("fertilizers.php")
    suspend fun deleteFertilizer(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // DISEASES
    @GET("diseases.php")
    suspend fun getDiseases(@Query("search") search: String? = null): Response<ApiResponse<List<CropDisease>>>

    @POST("diseases.php")
    suspend fun addDisease(@Body disease: CropDisease): Response<ApiResponse<Map<String, Any>>>

    // PRODUCTS
    @GET("products.php")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<List<MarketplaceProduct>>>

    @POST("products.php")
    suspend fun addProduct(@Body product: MarketplaceProduct): Response<ApiResponse<Map<String, Any>>>

    @DELETE("products.php")
    suspend fun deleteProduct(@Query("id") id: Int): Response<ApiResponse<Map<String, Any>>>

    // ORDERS
    @GET("orders.php")
    suspend fun getOrders(@Query("user_id") userId: Int? = null): Response<ApiResponse<List<Order>>>

    @POST("orders.php")
    suspend fun placeOrder(@Body order: CreateOrderRequest): Response<ApiResponse<Map<String, Any>>>

    // WEATHER
    @GET("weather.php")
    suspend fun getWeather(@Query("location") location: String? = null): Response<ApiResponse<WeatherResponseData>>

    // REPORTS
    @GET("reports.php")
    suspend fun getReports(): Response<ApiResponse<ReportsResponseData>>
}
