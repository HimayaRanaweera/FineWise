package com.example.finewise.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.finewise.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object FileUtils {
    private const val FILE_NAME = "finewise_backup.json"

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