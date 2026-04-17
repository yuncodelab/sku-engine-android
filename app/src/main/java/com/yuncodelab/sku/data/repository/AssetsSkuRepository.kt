package com.yuncodelab.sku.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yuncodelab.sku.data.remote.ApiResponse
import com.yuncodelab.sku.core.model.result.SpecResponse
import com.yuncodelab.sku.core.utils.SkuLogger

/**
 * Assets 数据源实现
 *
 * 当前用于：
 * - Demo 模拟接口返回
 * - 本地静态 SKU 数据读取
 *
 * 后续扩展：
 * - 可替换为网络 Repository
 * - 可增加缓存策略
 */
class AssetsSkuRepository(
    private val context: Context
) : SkuRepository {

    companion object {
        private const val TAG = "AssetsSkuRepository"
        private const val DEFAULT_FILE = "sku.json"
    }

    private val gson = Gson()

    override suspend fun loadSpec(): SpecResponse {
        SkuLogger.d(TAG, "开始加载 SKU 数据")

        return try {
            val json = readJsonFromAssets(DEFAULT_FILE)

            // 关键点：使用 TypeToken 获取带泛型的 Type
            val type = object : TypeToken<ApiResponse<SpecResponse>>() {}.type
            val apiResponse: ApiResponse<SpecResponse> = gson.fromJson(json, type)

            // 处理业务逻辑：判断 code 是否为 200
            if (apiResponse.code == 200 && apiResponse.data != null) {
                val response = apiResponse.data
                SkuLogger.d(
                    TAG,
                    "SKU 数据加载成功 specCount=${response.specList.size} skuCount=${response.skuList.size}"
                )
                response
            } else {
                val errorMsg = "业务逻辑异常: code=${apiResponse.code}, message=${apiResponse.message}"
                SkuLogger.e(TAG, errorMsg)
                throw IllegalStateException(errorMsg)
            }

        } catch (e: Exception) {
            SkuLogger.e(TAG, "SKU 数据加载失败", e)
            throw e
        }
    }

    /**
     * 从 Assets 读取 JSON 文件
     */
    private fun readJsonFromAssets(fileName: String): String {

        SkuLogger.d(TAG, "读取 assets 文件 -> $fileName")

        return context.assets.open(fileName).use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        }
    }
}