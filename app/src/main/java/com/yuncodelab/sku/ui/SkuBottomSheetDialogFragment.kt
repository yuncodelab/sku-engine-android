package com.yuncodelab.sku.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yuncodelab.sku.R
import com.yuncodelab.sku.core.model.result.SkuResult
import com.yuncodelab.sku.databinding.DialogSkuBinding
import com.yuncodelab.sku.ui.adapter.SpecAdapter
import com.yuncodelab.sku.viewmodel.SkuViewModel
import kotlinx.coroutines.launch

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

    private var _binding: DialogSkuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SkuViewModel by activityViewModels()

    private lateinit var specAdapter: SpecAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSkuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 并行收集两个 Flow
                launch {
                    viewModel.specUiList.collect { list ->
                        specAdapter.submitList(list)
                    }
                }

                launch {
                    viewModel.selectedSku.collect { sku ->
                        updateSkuUi(sku)
                    }
                }
            }
        }

        viewModel.initSku()
    }

    private fun updateSkuUi(sku: SkuResult?) {
        if (sku != null) {
            binding.tvPrice.text = "¥ ${sku.price}"
            val displayStr = sku.specs.joinToString(" · ") { "${it.specName}:${it.valueName}" }
            binding.tvSelectedSpec.text = "已选：$displayStr"
            binding.btnConfirm.isEnabled = true
        } else {
            binding.tvPrice.text = "¥ 0.00"
            binding.tvSelectedSpec.text = "请选择规格"
            binding.btnConfirm.isEnabled = false
        }
    }

    private fun setupRecyclerView() {
        specAdapter = SpecAdapter { specId, valueId ->
            viewModel.selectSpec(specId, valueId)
        }
        binding.rvSpec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = specAdapter
            itemAnimator = null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
            val maxHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
            it.layoutParams.height = maxHeight
            it.requestLayout()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = true
        }
    }
}