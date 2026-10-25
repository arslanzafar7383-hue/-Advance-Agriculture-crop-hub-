package com.example.utils

object Constants {
    // Default Android Emulator loopback to host PC XAMPP Apache
    const val DEFAULT_BASE_URL = "http://10.0.2.2/crop_hub_api/api/"
    
    // Fallback LAN IP example
    const val LAN_HINT_URL = "http://192.168.1.X/crop_hub_api/api/"

    // User types
    const val TYPE_FARMER = "Farmer"
    const val TYPE_DEALER = "Crop Dealer"
    const val TYPE_EXPERT = "Agriculture Expert"
    const val TYPE_STUDENT = "Student"

    val USER_TYPES = listOf(TYPE_FARMER, TYPE_DEALER, TYPE_EXPERT, TYPE_STUDENT)
    
    val CROP_CATEGORIES = listOf("All", "Cereals", "Cash Crops", "Legumes", "Vegetables", "Fruits", "Oilseeds")
    
    val FERTILIZER_TYPES = listOf("All", "Nitrogenous", "Phosphatic", "Potassic", "Organic", "Bio-Fertilizer")
    
    val MARKET_CATEGORIES = listOf(
        "All",
        "Seeds",
        "Fertilizers",
        "Farming Tools",
        "Irrigation Equipment",
        "Pesticides",
        "Agricultural Machinery"
    )
}
