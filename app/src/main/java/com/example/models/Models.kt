package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String = "success",
    @Json(name = "message") val message: String = "",
    @Json(name = "data") val data: T? = null
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "user_type") val userType: String = "Farmer",
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Farmer(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "farm_size_acres") val farmSizeAcres: Double = 0.0,
    @Json(name = "experience_years") val experienceYears: Int = 1,
    @Json(name = "registered_date") val registeredDate: String = "",
    @Json(name = "field_count") val fieldCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class Crop(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "crop_name") val cropName: String = "",
    @Json(name = "category") val category: String = "Cereals",
    @Json(name = "scientific_name") val scientificName: String = "",
    @Json(name = "season") val season: String = "Rabi",
    @Json(name = "soil_type") val soilType: String = "",
    @Json(name = "water_requirement") val waterRequirement: String = "Medium",
    @Json(name = "expected_yield_per_acre") val expectedYieldPerAcre: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class FieldEntity(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "field_name") val fieldName: String = "",
    @Json(name = "farmer_id") val farmerId: Int = 0,
    @Json(name = "farmer_name") val farmerName: String? = null,
    @Json(name = "location") val location: String = "",
    @Json(name = "area_acres") val areaAcres: Double = 0.0,
    @Json(name = "soil_type") val soilType: String = "",
    @Json(name = "irrigation_type") val irrigationType: String = "Canal",
    @Json(name = "current_crop_id") val currentCropId: Int? = null,
    @Json(name = "current_crop_name") val currentCropName: String? = null
)

@JsonClass(generateAdapter = true)
data class CropProduction(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "farmer_id") val farmerId: Int = 0,
    @Json(name = "farmer_name") val farmerName: String? = null,
    @Json(name = "field_id") val fieldId: Int = 0,
    @Json(name = "field_name") val fieldName: String? = null,
    @Json(name = "crop_id") val cropId: Int = 0,
    @Json(name = "crop_name") val cropName: String? = null,
    @Json(name = "planting_date") val plantingDate: String = "",
    @Json(name = "harvest_date") val harvestDate: String = "",
    @Json(name = "quantity_produced_kg") val quantityProducedKg: Double = 0.0,
    @Json(name = "production_cost") val productionCost: Double = 0.0,
    @Json(name = "expected_revenue") val expectedRevenue: Double = 0.0,
    @Json(name = "net_profit") val netProfit: Double? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class Fertilizer(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "type") val type: String = "Nitrogenous",
    @Json(name = "suitable_crops") val suitableCrops: String = "",
    @Json(name = "recommended_quantity") val recommendedQuantity: String = "",
    @Json(name = "price_per_unit") val pricePerUnit: Double = 0.0,
    @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class CropDisease(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "disease_name") val diseaseName: String = "",
    @Json(name = "affected_crops") val affectedCrops: String = "",
    @Json(name = "symptoms") val symptoms: String = "",
    @Json(name = "prevention") val prevention: String = "",
    @Json(name = "treatment_method") val treatmentMethod: String = ""
)

@JsonClass(generateAdapter = true)
data class MarketplaceProduct(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "category") val category: String = "Seeds",
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "seller_name") val sellerName: String = "",
    @Json(name = "seller_contact") val sellerContact: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "stock_quantity") val stockQuantity: Int = 10
)

@JsonClass(generateAdapter = true)
data class CartItem(
    val product: MarketplaceProduct,
    var quantity: Int = 1
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "order_id") val orderId: Int = 0,
    @Json(name = "product_id") val productId: Int = 0,
    @Json(name = "product_name") val productName: String = "",
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "unit_price") val unitPrice: Double = 0.0,
    @Json(name = "subtotal") val subtotal: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "customer_name") val customerName: String = "",
    @Json(name = "customer_phone") val customerPhone: String = "",
    @Json(name = "delivery_address") val deliveryAddress: String = "",
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "status") val status: String = "Confirmed",
    @Json(name = "order_date") val orderDate: String = "",
    @Json(name = "items") val items: List<OrderItem>? = null
)

@JsonClass(generateAdapter = true)
data class WeatherRecord(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "location_name") val locationName: String = "Farm Region",
    @Json(name = "temperature_c") val temperatureC: Double = 28.0,
    @Json(name = "humidity_pct") val humidityPct: Int = 50,
    @Json(name = "rain_probability_pct") val rainProbabilityPct: Int = 10,
    @Json(name = "wind_speed_kph") val windSpeedKph: Double = 12.0,
    @Json(name = "condition_text") val conditionText: String = "Clear Sunny",
    @Json(name = "recorded_at") val recordedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class ForecastDay(
    @Json(name = "day") val day: String = "",
    @Json(name = "temp_max") val tempMax: Int = 30,
    @Json(name = "temp_min") val tempMin: Int = 18,
    @Json(name = "condition") val condition: String = "",
    @Json(name = "rain_chance") val rainChance: Int = 10
)

@JsonClass(generateAdapter = true)
data class WeatherResponseData(
    @Json(name = "current") val current: WeatherRecord? = null,
    @Json(name = "forecast") val forecast: List<ForecastDay>? = null
)

@JsonClass(generateAdapter = true)
data class ReportTotals(
    @Json(name = "total_farmers") val totalFarmers: Int = 0,
    @Json(name = "total_crops") val totalCrops: Int = 0,
    @Json(name = "total_fields") val totalFields: Int = 0,
    @Json(name = "total_products") val totalProducts: Int = 0,
    @Json(name = "total_orders") val totalOrders: Int = 0,
    @Json(name = "total_production_kg") val totalProductionKg: Double = 0.0,
    @Json(name = "total_cost") val totalCost: Double = 0.0,
    @Json(name = "total_revenue") val totalRevenue: Double = 0.0,
    @Json(name = "net_profit") val netProfit: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CropProductionStat(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "crop_name") val cropName: String = "",
    @Json(name = "category") val category: String = "",
    @Json(name = "total_kg") val totalKg: Double = 0.0,
    @Json(name = "total_revenue") val totalRevenue: Double = 0.0,
    @Json(name = "harvest_count") val harvestCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class FarmerProductionStat(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "farm_size_acres") val farmSizeAcres: Double = 0.0,
    @Json(name = "total_kg") val totalKg: Double = 0.0,
    @Json(name = "total_revenue") val totalRevenue: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ReportsResponseData(
    @Json(name = "totals") val totals: ReportTotals = ReportTotals(),
    @Json(name = "crop_wise_production") val cropWiseProduction: List<CropProductionStat> = emptyList(),
    @Json(name = "farmer_wise_production") val farmerWiseProduction: List<FarmerProductionStat> = emptyList()
)

// DTOs for requests
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String,
    @Json(name = "user_type") val userType: String
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "delivery_address") val deliveryAddress: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "items") val items: List<OrderLineItemRequest>
)

@JsonClass(generateAdapter = true)
data class OrderLineItemRequest(
    @Json(name = "product_id") val productId: Int,
    @Json(name = "product_name") val productName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double
)
