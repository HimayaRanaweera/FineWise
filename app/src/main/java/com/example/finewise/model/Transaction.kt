package com.example.finewise.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val isIncome: Boolean
) : Parcelable