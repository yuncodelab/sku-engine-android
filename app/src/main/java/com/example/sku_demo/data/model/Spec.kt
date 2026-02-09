package com.example.sku_demo.data.model

/**
 * 商品规格模型
 *
 * 示例：
 * 颜色、尺寸、容量等
 *
 */
data class Spec(
    val specId: String,         // 规格ID
    val specName: String,       // 规格名称
    val values: List<SpecValue> // 当前规格下所有可选规格值
)

/**
 * 规格值业务模型
 *
 * 示例：
 * 红色、蓝色、XL 等
 *
 */
data class SpecValue(
    val id: String,
    val name: String,
    var enabled: Boolean = true
){

}