package com.example.finewise.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.example.finewise.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object FileUtils {
    private const val FILE_NAME = "finewise_backup.json"
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun exportTransactionsAsText(context: Context, transactions: List<Transaction>): String? {
        if (!hasStoragePermission(context)) return null
        try {
            val fileName = "FineWise_Transactions_${System.currentTimeMillis()}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            val content = buildString {
                appendLine("FineWise - Transaction Report")
                appendLine("Generated on: ${dateFormatter.format(System.currentTimeMillis())}")
                appendLine("----------------------------------------")
                appendLine()

                // Summary section
                val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
                val totalExpenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
                val netBalance = totalIncome - totalExpenses

                appendLine("SUMMARY")
                appendLine("Total Income: ${currencyFormatter.format(totalIncome)}")
                appendLine("Total Expenses: ${currencyFormatter.format(totalExpenses)}")
                appendLine("Net Balance: ${currencyFormatter.format(netBalance)}")
                appendLine("----------------------------------------")
                appendLine()

                // Transactions section
                appendLine("DETAILED TRANSACTIONS")
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    appendLine("Date: ${dateFormatter.format(transaction.date)}")
                    appendLine("Title: ${transaction.title}")
                    appendLine("Category: ${transaction.category}")
                    appendLine("Amount: ${currencyFormatter.format(transaction.amount)}")
                    appendLine("Type: ${if (transaction.isIncome) "Income" else "Expense"}")
                    appendLine("----------------------------------------")
                }
            }

            file.writeText(content)
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportTransactions(context: Context, transactions: List<Transaction>): Boolean {
        if (!hasStoragePermission(context)) return false
        try {
            val json = Gson().toJson(transactions)
            val file = File(context.getExternalFilesDir(null), FILE_NAME)
            file.writeText(json)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun importTransactions(context: Context): List<Transaction> {
        if (!hasStoragePermission(context)) return emptyList()
        try {
            val file = File(context.getExternalFilesDir(null), FILE_NAME)
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<Transaction>>() {}.type
            return Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}