package com.example.sku_demo.ui

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sku_demo.R
import com.example.sku_demo.adapter.SpecAdapter
import com.example.sku_demo.utils.SkuLogger
import com.example.sku_demo.viewmodel.SkuViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * SKU 选择弹窗
 *
 * 职责：
 * 1. 展示规格列表
 * 2. 响应用户点击行为
 * 3. 监听 ViewModel 状态更新 UI
 *
 * 注意：
 * - 仅负责 UI 渲染
 * - 不参与任何 SKU 计算逻辑
 */
class SkuBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "SkuDialog"
    }

    private val viewModel: SkuViewModel by activityViewModels()

    private lateinit var specAdapter: SpecAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        SkuLogger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.dialog_sku, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        SkuLogger.d(TAG, "onViewCreated 初始化UI")

        val rvSpec = view.findViewById<RecyclerView>(R.id.rvSpec)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        // ================= Adapter =================

        specAdapter = SpecAdapter { specId, valueId ->
            SkuLogger.d(TAG, "点击规格 specId=$specId valueId=$valueId")
            viewModel.selectSpec(specId, valueId)
        }

        rvSpec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = specAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        // ================= 监听状态 =================

        viewModel.specUiList.observe(viewLifecycleOwner) {
            SkuLogger.d(TAG, "刷新规格UI size=${it.size}")
            specAdapter.submitList(it)
        }

        viewModel.selectedSku.observe(viewLifecycleOwner) { sku ->

            SkuLogger.d(TAG, "当前选中SKU=${sku?.skuId}")

            tvPrice.text =
                sku?.let { "¥ ${it.price}" }
                    ?: "请选择规格"
        }

        viewModel.isAllSpecSelected.observe(viewLifecycleOwner) { selected ->

            SkuLogger.d(TAG, "是否规格选全=$selected")

            btnConfirm.isEnabled = selected
        }

        viewModel.initSku()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        SkuLogger.d(TAG, "创建BottomSheetDialog")
        return BottomSheetDialog(requireContext(), R.style.SkuBottomSheetDialog)
    }

    /**
     * 固定 BottomSheet 高度
     */
    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.let {

            val behavior = BottomSheetBehavior.from(it)

            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.6).toInt()

            SkuLogger.d(TAG, "设置BottomSheet高度=$maxHeight")

            it.layoutParams.height = maxHeight
            it.requestLayout()

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
        }
    }
}