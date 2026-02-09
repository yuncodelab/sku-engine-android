package com.example.sku_demo.data.enum

/**
 * @author zhiyunliu
 * @desc 规格值 UI 状态枚举
 * 用于控制规格值在界面中的可选状态。
 */
enum class SpecValueStatus {
    ENABLED,    // 可选
    DISABLED,   // 不可选（规格冲突）
    OUT_OF_STOCK, // 组合存在但库存为 0
    SELECTED    // 已选中
}