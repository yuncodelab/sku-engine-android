package com.yuncodelab.sku.core.model.result

import com.yuncodelab.sku.core.model.entity.Sku
import com.yuncodelab.sku.core.model.entity.Spec

/**
 * SKU 接口返回模型
 *
 * 通常由服务端返回，用于初始化 SKU 选择模块。
 *
 */
data class SpecResponse(
    val specList: List<Spec>,   // 规格列表
    val skuList: List<Sku>,     // SKU 列表
    val defaultSkuId: String?   // 默认选中 SKU（可为空）
)