package com.yuncodelab.sku.data.remote

/**
 * 统一接口响应包装类
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)