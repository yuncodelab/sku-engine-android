package com.yuncodelab.sku.core.model.result

import com.yuncodelab.sku.core.model.constant.SpecValueStatus

/**
 * 规格 UI 模型
 *
 * 表示单个规格（如颜色、尺寸）在界面中的展示结构。
 *
 */
data class SpecResult(
    val specId: String,             // 规格ID
    val specName: String,           // 规格名称
    val values: List<SpecValueResult>   // 当前规格下所有规格值 UI 数据
)

/**
 * 规格值 UI 模型
 *
 * 表示规格下某个具体选项（如红色、XL）。
 *
 */
data class SpecValueResult(
    val id: String,             // 规格值ID
    val name: String,           // 显示名称
    val status: SpecValueStatus // 当前 UI 状态
)