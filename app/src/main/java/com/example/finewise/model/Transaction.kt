package com.example.finewise.model

import java.io.Serializable
import java.util.Date

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val isIncome: Boolean
) : Serializable