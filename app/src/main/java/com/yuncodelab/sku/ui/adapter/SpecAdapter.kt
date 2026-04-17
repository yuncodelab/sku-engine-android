package com.yuncodelab.sku.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.yuncodelab.sku.R
import com.yuncodelab.sku.core.model.result.SpecResult
import com.yuncodelab.sku.databinding.ItemSpecBinding

/**
 * 规格分类 Adapter
 *
 * 结构：
 * RecyclerView
 *   └── 每个规格一行
 *           └── 内部 Flexbox RecyclerView
 */
class SpecAdapter(
    private val onSpecValueClick: (specId: String, valueId: String) -> Unit
) : ListAdapter<SpecResult, SpecAdapter.SpecVH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecVH {
        val binding = ItemSpecBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SpecVH(binding)
    }

    override fun onBindViewHolder(holder: SpecVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SpecVH(
        private val binding: ItemSpecBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val valueAdapter = SpecValueAdapter("", onSpecValueClick)

        init {
            binding.rvSpecValue.apply {
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
                adapter = valueAdapter
                itemAnimator = null
                setHasFixedSize(true)
            }
        }

        fun bind(spec: SpecResult) {
            binding.tvSpecName.text = spec.specName

            valueAdapter.updateSpecId(spec.specId)
            valueAdapter.submitList(spec.values)
        }
    }

    companion object {
        val Diff = object : DiffUtil.ItemCallback<SpecResult>() {
            override fun areItemsTheSame(old: SpecResult, new: SpecResult) =
                old.specId == new.specId

            override fun areContentsTheSame(old: SpecResult, new: SpecResult) =
                old == new
        }
    }
}