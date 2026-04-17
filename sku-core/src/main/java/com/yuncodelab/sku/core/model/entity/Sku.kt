package com.yuncodelab.sku.core.model.entity


/**
 * SKU 业务模型
 *
 * 示例：
 * 红色 + XL → 唯一 SKU
 *
 */
data class Sku(
    val skuId: String,              // SKU 唯一标识
    val price: Double,              // 当前价格
    val stock: Int,                 // 当前库存数量
    val specs: Map<String, String>  // 规格组合（specId -> valueId）
)