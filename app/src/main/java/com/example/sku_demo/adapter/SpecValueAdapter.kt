package com.example.sku_demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sku_demo.R
import com.example.sku_demo.data.enum.SpecValueStatus
import com.example.sku_demo.data.model.ui.SpecValueUI
import com.example.sku_demo.utils.SkuLogger

/**
 * 规格值 Adapter
 *
 * 职责：
 * 1. 渲染规格值状态
 * 2. 处理点击事件
 */
class SpecValueAdapter(
    private val specId: String,
    private val onClick: (specId: String, valueId: String) -> Unit
) : ListAdapter<SpecValueUI, SpecValueAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spec_value, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        private val tvOutOfStock: TextView = itemView.findViewById(R.id.tvOutOfStock)

        fun bind(value: SpecValueUI) {
            tvValue.text = value.name


            when (value.status) {
                SpecValueStatus.SELECTED -> {
                    tvValue.isEnabled = true
                    tvValue.isSelected = true
                    tvOutOfStock.visibility = View.GONE
                }
                SpecValueStatus.ENABLED -> {
                    tvValue.isEnabled = true
                    tvValue.isSelected = false
                    tvOutOfStock.visibility = View.GONE
                }
                SpecValueStatus.OUT_OF_STOCK -> {
                    tvValue.isEnabled = false
                    tvValue.isSelected = false
                    tvOutOfStock.visibility = View.VISIBLE
                }
                SpecValueStatus.DISABLED -> {
                    tvValue.isEnabled = false
                    tvValue.isSelected = false
                    tvOutOfStock.visibility = View.GONE
                }
            }

            tvValue.setOnClickListener {
                SkuLogger.d(
                    TAG,
                    "点击规格值 specId=$specId valueId=${value.id}"
                )
                if (value.status == SpecValueStatus.ENABLED ||
                    value.status == SpecValueStatus.SELECTED
                ) {
                    onClick(specId, value.id)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SpecValueAdapter"

        val Diff = object : DiffUtil.ItemCallback<SpecValueUI>() {
            override fun areItemsTheSame(old: SpecValueUI, new: SpecValueUI) =
                old.id == new.id

            override fun areContentsTheSame(old: SpecValueUI, new: SpecValueUI) =
                old == new
        }
    }
}