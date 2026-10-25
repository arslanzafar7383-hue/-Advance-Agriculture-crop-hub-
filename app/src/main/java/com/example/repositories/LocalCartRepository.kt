package com.example.repositories

import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import com.example.models.CartItem
import com.example.models.MarketplaceProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCartRepository(private val cartDao: CartDao) {

    val cartItemsFlow: Flow<List<CartItem>> = cartDao.getAllCartItems().map { list ->
        list.map { entity ->
            CartItem(
                product = MarketplaceProduct(
                    id = entity.productId,
                    name = entity.productName,
                    category = entity.category,
                    price = entity.price,
                    sellerName = entity.sellerName,
                    sellerContact = "",
                    description = "",
                    imageUrl = entity.imageUrl,
                    stockQuantity = 99
                ),
                quantity = entity.quantity
            )
        }
    }

    suspend fun addToCart(product: MarketplaceProduct, quantityToAdd: Int = 1) {
        val existing = cartDao.getCartItemById(product.id)
        if (existing != null) {
            val newQty = existing.quantity + quantityToAdd
            cartDao.updateQuantity(product.id, newQty)
        } else {
            cartDao.insertOrUpdate(
                CartItemEntity(
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    price = product.price,
                    sellerName = product.sellerName,
                    imageUrl = product.imageUrl,
                    quantity = quantityToAdd
                )
            )
        }
    }

    suspend fun updateQuantity(productId: Int, change: Int) {
        val existing = cartDao.getCartItemById(productId) ?: return
        val newQty = existing.quantity + change
        if (newQty <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, newQty)
        }
    }

    suspend fun removeFromCart(productId: Int) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}
