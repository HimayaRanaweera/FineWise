package com.example.finewise.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.ItemCategorySummaryBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(private val categories: List<CategorySummary>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

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