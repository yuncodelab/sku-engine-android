package com.example.sku_demo.data.repository

import android.content.Context
import com.example.sku_demo.data.model.SpecResponse
import com.example.sku_demo.utils.SkuLogger
import com.google.gson.Gson

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

    override fun loadSpec(): SpecResponse {
        SkuLogger.d(TAG, "开始加载 SKU 数据")

        return try {
            val json = readJsonFromAssets(DEFAULT_FILE)

            val response = gson.fromJson(json, SpecResponse::class.java)

            SkuLogger.d(
                TAG,
                "SKU 数据加载成功 specCount=${response.specList.size} skuCount=${response.skuList.size}"
            )

            response

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