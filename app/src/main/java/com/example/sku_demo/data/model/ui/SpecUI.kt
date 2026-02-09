package com.example.sku_demo.data.model.ui

/**
 * 规格 UI 模型
 *
 * 表示单个规格（如颜色、尺寸）在界面中的展示结构。
 *
 */
data class SpecUI(
    val specId: String,             // 规格ID
    val specName: String,           // 规格名称
    val values: List<SpecValueUI>   // 当前规格下所有规格值 UI 数据
)