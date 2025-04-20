package com.example.finewise.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.CategoryItemBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(private val summaries: List<CategorySummary>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    inner class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = summaries[position]
        with(holder.binding) {
            tvCategory.text = summary.category
            tvTotal.text = currencyFormatter.format(summary.total)
            tvCount.text = "${summary.count} transactions"
            
            // Add a progress bar or visual indicator of the amount
            progressBar.max = 100
            val maxAmount = summaries.maxByOrNull { it.total }?.total ?: summary.total
            val percentage = ((summary.total / maxAmount) * 100).toInt()
            progressBar.progress = percentage
        }
    }

    override fun getItemCount(): Int = summaries.size
}