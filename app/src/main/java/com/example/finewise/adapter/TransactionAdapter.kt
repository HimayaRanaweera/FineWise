package com.example.finewise.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.TransactionItemBinding
import com.example.finewise.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: TransactionItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TransactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        with(holder.binding) {
            tvTitle.text = transaction.title
            tvAmount.text = if (transaction.isIncome) "+$${transaction.amount}" else "-$${transaction.amount}"
            tvCategory.text = transaction.category
            tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.date)
            btnEdit.setOnClickListener { onEdit(transaction) }
            btnDelete.setOnClickListener { onDelete(transaction) }
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun update(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}