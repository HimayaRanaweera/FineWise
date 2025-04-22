package com.example.finewise.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.ItemCategorySummaryBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private val categories: List<CategorySummary>,
    private var currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance()
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    fun updateCurrencyFormatter(formatter: NumberFormat) {
        currencyFormatter = formatter
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCategorySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: CategorySummary) {
            binding.apply {
                tvCategory.text = item.category
                tvAmount.text = currencyFormatter.format(item.total)
                tvCount.text = "${item.count} transactions"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategorySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}