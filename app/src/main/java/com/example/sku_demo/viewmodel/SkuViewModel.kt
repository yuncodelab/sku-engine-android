package com.example.sku_demo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sku_demo.data.model.Sku
import com.example.sku_demo.data.model.ui.SpecUI
import com.example.sku_demo.data.repository.AssetsSkuRepository
import com.example.sku_demo.data.repository.SkuRepository
import com.example.sku_demo.engine.SkuEngine
import com.example.sku_demo.utils.SkuLogger

/**
 * SKU 规格选择 ViewModel
 *
 * 职责：
 * 1. 负责加载 SKU 数据
 * 2. 管理 SkuEngine 生命周期
 * 3. 对外暴露 UI 层需要的数据流
 * 4. 处理用户规格选择事件
 *
 * 架构定位：
 * UI -> ViewModel -> Engine -> Repository
 *
 * 注意：
 * - ViewModel 不参与规格计算
 * - 所有业务规则由 SkuEngine 负责
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
    private val repository: SkuRepository =
        AssetsSkuRepository(application)

    /**
     * SKU 业务引擎
     * 负责规格计算逻辑
     */
    private lateinit var skuEngine: SkuEngine

    // ================= UI State =================

    /**
     * 规格 UI 数据
     * 用于 RecyclerView 展示
     */
    private val _specUiList = MutableLiveData<List<SpecUI>>()
    val specUiList: LiveData<List<SpecUI>> = _specUiList

    /**
     * 当前选中的 SKU
     */
    private val _selectedSku = MutableLiveData<Sku?>()
    val selectedSku: LiveData<Sku?> = _selectedSku

    /**
     * 是否已选择全部规格
     * 用于控制确认按钮状态
     */
    private val _isAllSpecSelected = MutableLiveData<Boolean>()
    val isAllSpecSelected: LiveData<Boolean> = _isAllSpecSelected

    // ================= 初始化 =================

    /**
     * 初始化 SKU 数据
     *
     * 执行流程：
     * 1. 从 Repository 加载数据
     * 2. 初始化 SkuEngine
     * 3. 生成 UI 规格状态
     * 4. 更新选中 SKU
     */
    fun initSku() {

        SkuLogger.d(TAG, "开始初始化 SKU")

        val response = repository.loadSpec()

        SkuLogger.d(
            TAG,
            "SKU 数据加载完成 specCount=${response.specList.size}, skuCount=${response.skuList.size}"
        )

        skuEngine = SkuEngine(
            specList = response.specList,
            skuList = response.skuList
        )

        // 初始化规格 UI
        val uiList = skuEngine.initSpecStatus()
        _specUiList.value = uiList

        updateSelectedSku()

        SkuLogger.d(TAG, "SKU 初始化完成 默认SKU=${skuEngine.getSelectedSku()?.skuId}")
    }

    // ================= 用户行为 =================

    /**
     * 用户点击规格值
     */
    fun selectSpec(specId: String, valueId: String) {

        SkuLogger.d(TAG, "用户点击规格 specId=$specId valueId=$valueId")

        val uiList = skuEngine.select(specId, valueId)

        _specUiList.value = uiList

        updateSelectedSku()
    }

    // ================= 内部状态同步 =================

    /**
     * 同步当前选中 SKU 状态
     */
    private fun updateSelectedSku() {

        val sku = skuEngine.getSelectedSku()

        _selectedSku.value = sku
        _isAllSpecSelected.value = skuEngine.isAllSpecSelected()

        SkuLogger.d(
            TAG,
            "更新选中状态 -> sku=${sku?.skuId}, allSelected=${_isAllSpecSelected.value}"
        )
    }
}