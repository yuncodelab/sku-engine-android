package com.yuncodelab.sku.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuncodelab.sku.core.model.constant.SpecValueStatus
import com.yuncodelab.sku.core.model.result.SpecValueResult
import com.yuncodelab.sku.databinding.ItemSpecValueBinding

/**
 * 规格值 Adapter
 *
 * 职责：
 * 1. 渲染规格值状态
 * 2. 处理点击事件
 */
class SpecValueAdapter(
    private var specId: String,
    private val onClick: (specId: String, valueId: String) -> Unit
) : ListAdapter<SpecValueResult, SpecValueAdapter.VH>(Diff) {

    fun updateSpecId(newSpecId: String) {
        specId = newSpecId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSpecValueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val binding: ItemSpecValueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(value: SpecValueResult) {
            binding.tvValue.text = value.name

            when (value.status) {
                SpecValueStatus.SELECTED -> {
                    binding.tvValue.isEnabled = true
                    binding.tvValue.isSelected = true
                    binding.tvOutOfStock.visibility = View.GONE
                }

                SpecValueStatus.ENABLED -> {
                    binding.tvValue.isEnabled = true
                    binding.tvValue.isSelected = false
                    binding.tvOutOfStock.visibility = View.GONE
                }

                SpecValueStatus.OUT_OF_STOCK -> {
                    binding.tvValue.isEnabled = false
                    binding.tvValue.isSelected = false
                    binding.tvOutOfStock.visibility = View.VISIBLE
                }

                SpecValueStatus.DISABLED -> {
                    binding.tvValue.isEnabled = false
                    binding.tvValue.isSelected = false
                    binding.tvOutOfStock.visibility = View.GONE
                }
            }

            binding.tvValue.setOnClickListener {
                if (value.status == SpecValueStatus.ENABLED ||
                    value.status == SpecValueStatus.SELECTED
                ) {
                    onClick(specId, value.id)
                }
            }
        }
    }

    companion object {
        val Diff = object : DiffUtil.ItemCallback<SpecValueResult>() {
            override fun areItemsTheSame(old: SpecValueResult, new: SpecValueResult) =
                old.id == new.id

            override fun areContentsTheSame(old: SpecValueResult, new: SpecValueResult) =
                old == new
        }
    }
}