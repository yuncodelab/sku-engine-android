package com.example.sku_demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sku_demo.R
import com.example.sku_demo.data.model.ui.SpecUI
import com.example.sku_demo.utils.SkuLogger
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

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
) : ListAdapter<SpecUI, SpecAdapter.SpecVH>(Diff) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecVH {
        SkuLogger.d(TAG, "创建SpecViewHolder")

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spec, parent, false)
        return SpecVH(view)
    }

    override fun onBindViewHolder(holder: SpecVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SpecVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSpecName: TextView = itemView.findViewById(R.id.tvSpecName)
        private val rvSpecValue: RecyclerView = itemView.findViewById(R.id.rvSpecValue)

        fun bind(spec: SpecUI) {
            tvSpecName.text = spec.specName

            val valueAdapter = SpecValueAdapter(spec.specId, onSpecValueClick)
            rvSpecValue.layoutManager =   FlexboxLayoutManager(itemView.context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            rvSpecValue.adapter = valueAdapter
            rvSpecValue.itemAnimator = null
            rvSpecValue.setHasFixedSize(true)
            valueAdapter.submitList(spec.values)
        }
    }

    companion object {
        private const val TAG = "SpecAdapter"
        val Diff = object : DiffUtil.ItemCallback<SpecUI>() {
            override fun areItemsTheSame(old: SpecUI, new: SpecUI) =
                old.specId == new.specId

            override fun areContentsTheSame(old: SpecUI, new: SpecUI) =
                old == new
        }
    }
}