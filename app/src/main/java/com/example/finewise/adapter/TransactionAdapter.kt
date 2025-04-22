package com.example.finewise.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.databinding.TransactionItemBinding
import com.example.finewise.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var currencyFormatter = NumberFormat.getCurrencyInstance()

    fun updateCurrencyFormatter(formatter: NumberFormat) {
        currencyFormatter = formatter
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TransactionItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TransactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get transaction from the end of the list to show newest first
        val transaction = transactions[transactions.size - 1 - position]
        with(holder.binding) {
            // Make the entire item clickable for editing
            root.setOnClickListener {
                onEdit(transaction)
            }

            // Set title
            tvTitle.text = transaction.title

            // Format and set amount with color
            val amount = currencyFormatter.format(transaction.amount)
            tvAmount.text = if (transaction.isIncome) "+$amount" else "-$amount"
            tvAmount.setTextColor(
                if (transaction.isIncome) Color.parseColor("#4CAF50")
                else Color.parseColor("#F44336")
            )

            // Set category chip
            chipCategory.text = transaction.category

            // Format and set date
            tvDate.text = dateFormatter.format(transaction.date)

            // Setup action buttons
            btnEdit.setOnClickListener {
                onEdit(transaction)
            }
            btnDelete.setOnClickListener {
                onDelete(transaction)
            }
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun update(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
        notifyItemInserted(0) // Add to the top of the list
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            notifyItemChanged(transactions.size - 1 - index) // Update the correct position
        }
    }

    fun removeTransaction(transaction: Transaction) {
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions.removeAt(index)
            notifyItemRemoved(transactions.size - index) // Remove from the correct position
        }
    }
}