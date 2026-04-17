package com.yuncodelab.sku.core.model.result

/**
 * SKU 选择的完整结果
 */
data class SkuResult(
    val skuId: String,
    val price: Double,
    val stock: Int,
    val specs: List<SelectedSpec>, // 将 Map 展平为包含 Name 的列表
)

/**
 * 选中的单个规格详情
 */
data class SelectedSpec(
    val specId: String,
    val specName: String,
    val valueId: String,
    val valueName: String
)