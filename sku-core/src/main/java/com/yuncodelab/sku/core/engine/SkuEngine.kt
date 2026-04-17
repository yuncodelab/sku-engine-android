package com.yuncodelab.sku.core.engine

import com.yuncodelab.sku.core.model.constant.SpecValueStatus
import com.yuncodelab.sku.core.model.entity.Sku
import com.yuncodelab.sku.core.model.entity.Spec
import com.yuncodelab.sku.core.model.result.SelectedSpec
import com.yuncodelab.sku.core.model.result.SkuResult
import com.yuncodelab.sku.core.model.result.SpecResult
import com.yuncodelab.sku.core.model.result.SpecValueResult
import com.yuncodelab.sku.core.utils.SkuLogger

/**
 * SKU 规格计算引擎
 *
 * 作用：
 * 1. 维护当前规格选择状态
 * 2. 计算每个规格值的可选状态
 * 3. 判断 SKU 是否完整选中
 * 4. 输出 UI 层使用的 SpecResult 结构
 *
 */
class SkuEngine(
    private val specList: List<Spec>,
    private val skuList: List<Sku>,
    private val defaultSkuId: String? = null
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
    fun initSpecStatus(): List<SpecResult> {
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
    fun select(specId: String, valueId: String): List<SpecResult> {

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
     * 获取当前选中的完整结果
     * * 逻辑：
     * 1. 检查是否选全
     * 2. 查找匹配的 Sku 实体
     * 3. 组装包含规格名称的 SkuResult
     */
    fun getSelectedSku(): SkuResult? {
        // 1. 校验是否选全
        if (!isAllSpecSelected()) return null

        // 2. 查找匹配的原始 Sku
        val matchedSku = skuList.firstOrNull { sku ->
            selectedSpecMap.all { (specId, valueId) ->
                sku.specs[specId] == valueId
            }
        } ?: return null

        // 3. 组装结果：将 ID 映射为名称
        // 这里利用已有的 specList 作为字典进行查找
        val selectedSpecList = specList.map { spec ->
            val valueId = selectedSpecMap[spec.specId] ?: ""
            val valueName = spec.values.find { it.id == valueId }?.name ?: ""
            SelectedSpec(
                specId = spec.specId,
                specName = spec.specName,
                valueId = valueId,
                valueName = valueName
            )
        }

        return SkuResult(
            skuId = matchedSku.skuId,
            price = matchedSku.price,
            stock = matchedSku.stock,
            specs = selectedSpecList,
        )
    }

    /**
     * 构建 UI 层规格结构
     */
    private fun buildSpecUI(): List<SpecResult> {

        return specList.map { spec ->
            SpecResult(
                specId = spec.specId,
                specName = spec.specName,
                values = spec.values.map { value ->
                    SpecValueResult(
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
     * 1. 优先根据服务端返回的 defaultSkuId 匹配
     * 2. 如果服务端给的默认 SKU 存在且可用，则直接使用
     * 3. 否则找第一个有库存的
     */
    fun initDefaultSelection() {
        // 1. 尝试寻找服务端指定的默认 SKU
        val serverDefaultSku = skuList.find { it.skuId == defaultSkuId }
        // 服务端默认 SKU 是否可用
        val serverDefaultSkuEnable = serverDefaultSku != null && serverDefaultSku.stock > 0

        // 2. 确定最终要使用的默认 SKU
        val targetSku = if (serverDefaultSkuEnable) {
            // 如果服务端给的默认 SKU 存在且可用，则直接使用
            serverDefaultSku
        } else {
            // 否则找第一个有库存的
            skuList.firstOrNull { it.stock > 0 }
        }

        targetSku?.let { sku ->
            SkuLogger.d(TAG, "默认选择的 SKU -> ${sku}")
            specList.forEach { spec ->
                sku.specs[spec.specId]?.let { valueId ->
                    selectedSpecMap[spec.specId] = valueId
                }
            }
        }
    }
}