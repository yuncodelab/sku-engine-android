package com.yuncodelab.sku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yuncodelab.sku.core.engine.SkuEngine
import com.yuncodelab.sku.core.model.result.SkuResult
import com.yuncodelab.sku.core.model.result.SpecResult
import com.yuncodelab.sku.core.utils.SkuLogger
import com.yuncodelab.sku.data.repository.AssetsSkuRepository
import com.yuncodelab.sku.data.repository.SkuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SKU 规格选择 ViewModel
 */
class SkuViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SkuViewModel"
    }

    /**
     * 数据仓库层
     * 负责加载 SKU 数据源
     */
    private val repository: SkuRepository = AssetsSkuRepository(application)

    /**
     * SKU 业务引擎
     * 负责规格计算逻辑
     */
    private lateinit var skuEngine: SkuEngine

    /**
     * 规格 UI 数据
     * 用于 RecyclerView 展示
     */
    private val _specUiList = MutableStateFlow<List<SpecResult>>(emptyList())
    val specUiList: StateFlow<List<SpecResult>> = _specUiList.asStateFlow()

    /**
     * 当前选中的 SKU
     */
    private val _selectedSku = MutableStateFlow<SkuResult?>(null)
    val selectedSku: StateFlow<SkuResult?> = _selectedSku.asStateFlow()

    /**
     * 初始化 SKU 数据
     */
    fun initSku() {
        viewModelScope.launch {
            val response = repository.loadSpec()

            skuEngine = SkuEngine(
                specList = response.specList,
                skuList = response.skuList,
                defaultSkuId = response.defaultSkuId
            )

            // 初始化规格 UI
            val uiList = skuEngine.initSpecStatus()
            _specUiList.value = uiList

            updateSelectedSku()
        }
    }

    /**
     * 用户点击规格值
     */
    fun selectSpec(specId: String, valueId: String) {
        // 如果引擎尚未初始化则直接返回
        if (!::skuEngine.isInitialized) return

        val uiList = skuEngine.select(specId, valueId)
        _specUiList.value = uiList

        updateSelectedSku()
    }

    /**
     * 同步当前选中 SKU 状态
     */
    private fun updateSelectedSku() {
        _selectedSku.value = skuEngine.getSelectedSku().apply {
            SkuLogger.d(TAG, "当前选中的 SKU -> $this")
        }
    }
}