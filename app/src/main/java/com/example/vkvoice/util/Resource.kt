package com.example.vkvoice.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Playing<T>: Resource<T>()
    class Pause<T>: Resource<T>()
    class Stop<T>: Resource<T>()

    class Error<T>(message: String?): Resource<T>(message = message)
    class Recording<T>: Resource<T>()
    class Done<T>(data: T): Resource<T>(data)
}