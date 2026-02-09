package com.example.sku_demo.engine

import com.example.sku_demo.data.enum.SpecValueStatus
import com.example.sku_demo.data.model.Sku
import com.example.sku_demo.data.model.Spec
import com.example.sku_demo.data.model.ui.SpecUI
import com.example.sku_demo.data.model.ui.SpecValueUI
import com.example.sku_demo.utils.SkuLogger

/**
 * SKU 规格计算引擎
 *
 * 作用：
 * 1. 维护当前规格选择状态
 * 2. 计算每个规格值的可选状态
 * 3. 判断 SKU 是否完整选中
 * 4. 输出 UI 层使用的 SpecUI 结构
 *
 * 设计原则：
 * - 纯业务逻辑类（不依赖 Android）
 * - 单一职责：只负责规格计算
 * - 对 UI 层完全透明
 */
class SkuEngine(
    private val specList: List<Spec>,
    private val skuList: List<Sku>
) {

    companion object {
        private const val TAG = "SkuEngine"
    }

    /** 当前已选规格：specId -> valueId */
    private val selectedSpecMap = mutableMapOf<String, String>()

    /**
     * 初始化规格状态
     *
     * 行为：
     * - 清空当前选择
     * - 初始化默认选中 SKU
     */
    fun initSpecStatus(): List<SpecUI> {
        SkuLogger.d(TAG, "初始化 SKU 规格状态")

        selectedSpecMap.clear()
        initDefaultSelection()

        val uiList = buildSpecUI()

        SkuLogger.d(TAG, "初始化完成 selectedSpecMap=$selectedSpecMap")

        return uiList
    }

    /**
     * 用户点击某个规格值
     */
    fun select(specId: String, valueId: String): List<SpecUI> {

        SkuLogger.d(TAG, "用户选择规格 specId=$specId valueId=$valueId")

        if (selectedSpecMap[specId] == valueId) {
            selectedSpecMap.remove(specId)
            SkuLogger.d(TAG, "取消选择 specId=$specId")
        } else {
            selectedSpecMap[specId] = valueId
            SkuLogger.d(TAG, "选中规格 specId=$specId valueId=$valueId")
        }

        val uiList = buildSpecUI()

        SkuLogger.d(TAG, "当前选择状态 -> $selectedSpecMap")

        return uiList
    }

    /**
     * 是否已选全所有规格
     */
    fun isAllSpecSelected(): Boolean {
        val result = selectedSpecMap.size == specList.size
        SkuLogger.d(TAG, "是否选全规格 -> $result")
        return result
    }

    /**
     * 获取当前选中的 SKU
     *
     * 注意：
     * 只有规格全部选中时才可能返回
     */
    fun getSelectedSku(): Sku? {

        if (!isAllSpecSelected()) {
            return null
        }

        val sku = skuList.firstOrNull { sku ->
            selectedSpecMap.all { (specId, valueId) ->
                sku.specs[specId] == valueId
            }
        }

        SkuLogger.d(TAG, "当前选中 SKU -> ${sku?.skuId}")

        return sku
    }

    // ================= 核心算法 =================

    /**
     * 构建 UI 层规格结构
     */
    private fun buildSpecUI(): List<SpecUI> {

        return specList.map { spec ->
            SpecUI(
                specId = spec.specId,
                specName = spec.specName,
                values = spec.values.map { value ->
                    SpecValueUI(
                        id = value.id,
                        name = value.name,
                        status = calculateStatus(spec.specId, value.id)
                    )
                }
            )
        }
    }

    /**
     * 计算某个规格值的状态
     *
     * 状态规则：
     * - SELECTED：当前已选
     * - DISABLED：不存在匹配 SKU
     * - OUT_OF_STOCK：存在 SKU 但库存为 0
     * - ENABLED：可选
     */
    private fun calculateStatus(specId: String, valueId: String): SpecValueStatus {

        if (selectedSpecMap[specId] == valueId) {
            return SpecValueStatus.SELECTED
        }

        val tempSelected = selectedSpecMap.toMutableMap()
        tempSelected[specId] = valueId

        val matchedSkus = skuList.filter { sku ->
            tempSelected.all { (sId, vId) ->
                sku.specs[sId] == vId
            }
        }

        if (matchedSkus.isEmpty()) {
            return SpecValueStatus.DISABLED
        }

        if (matchedSkus.all { it.stock <= 0 }) {
            return SpecValueStatus.OUT_OF_STOCK
        }

        return SpecValueStatus.ENABLED
    }

    /**
     * 默认选中 SKU
     *
     * 策略：
     * 1. 优先选有库存 SKU
     * 2. 如果没有库存，则选第一个 SKU
     */
    fun initDefaultSelection() {

        val defaultSku = skuList.firstOrNull { it.stock > 0 }
            ?: skuList.firstOrNull()
            ?: return

        SkuLogger.d(TAG, "默认选中 SKU -> ${defaultSku.skuId}")

        specList.forEach { spec ->
            defaultSku.specs[spec.specId]?.let { valueId ->
                selectedSpecMap[spec.specId] = valueId
            }
        }
    }
}