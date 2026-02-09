package com.example.sku_demo.data.model.ui

import com.example.sku_demo.data.enum.SpecValueStatus

/**
 * 规格值 UI 模型
 *
 * 表示规格下某个具体选项（如红色、XL）。
 *
 */
data class SpecValueUI(
    val id: String,             // 规格值ID
    val name: String,           // 显示名称
    val status: SpecValueStatus // 当前 UI 状态
)