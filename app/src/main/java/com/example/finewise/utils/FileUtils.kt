package com.example.finewise.utils

import android.content.Context
import com.example.finewise.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object FileUtils {
    private const val FILE_NAME = "finewise_backup.json"

    fun exportTransactions(context: Context, transactions: List<Transaction>) {
        val json = Gson().toJson(transactions)
        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        file.writeText(json)
    }

    fun importTransactions(context: Context): List<Transaction> {
        val file = File(context.getExternalFilesDir(null), FILE_NAME)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }
}