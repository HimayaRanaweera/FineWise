package com.example.finewise.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.CategoryItemBinding

data class CategorySummary(val category: String, val total: Double)

class CategoryAdapter(private val summaries: List<CategorySummary>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = summaries[position]
        with(holder.binding) {
            tvCategory.text = summary.category
            tvTotal.text = "$${String.format("%.2f", summary.total)}"
        }
    }

    override fun getItemCount(): Int = summaries.size
}