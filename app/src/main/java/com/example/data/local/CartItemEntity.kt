package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val productId: Int,
    val productName: String,
    val category: String,
    val price: Double,
    val sellerName: String,
    val imageUrl: String?,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)
