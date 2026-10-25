package com.example.utils

sealed class Resource<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : Resource<T>()
    data class Error<out T>(val message: String, val data: T? = null) : Resource<T>()
    object Loading : Resource<Nothing>()
}
