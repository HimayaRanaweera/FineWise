package com.example.finewise.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finewise.databinding.FragmentAnalysisBinding
import com.example.finewise.model.Transaction
import com.example.finewise.utils.PrefsUtils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.util.Locale

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        updateData()
    }

    private fun setupCharts() {
        setupPieChart(binding.incomeChart, "Income by Category")
        setupPieChart(binding.expenseChart, "Expense by Category")
    }

    private fun setupPieChart(chart: PieChart, centerText: String) {
        chart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            this.centerText = centerText
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = true
            legend.textSize = 12f
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun updateData() {
        val transactions = PrefsUtils.getTransactions(requireContext())
        val incomeTransactions = transactions.filter { it.isIncome }
        val expenseTransactions = transactions.filter { !it.isIncome }

        // Update summary
        val totalIncome = incomeTransactions.sumOf { it.amount }
        val totalExpenses = expenseTransactions.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binding.tvTotalIncome.text = currencyFormatter.format(totalIncome)
        binding.tvTotalExpenses.text = currencyFormatter.format(totalExpenses)
        binding.tvBalance.text = currencyFormatter.format(balance)
        binding.tvBalance.setTextColor(
            if (balance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )

        updatePieChart(binding.incomeChart, incomeTransactions, binding.tvNoIncome, true)
        updatePieChart(binding.expenseChart, expenseTransactions, binding.tvNoExpense, false)
    }

    private fun updatePieChart(
        chart: PieChart,
        transactions: List<Transaction>,
        noDataText: View,
        isIncome: Boolean
    ) {
        if (transactions.isEmpty()) {
            chart.visibility = View.GONE
            noDataText.visibility = View.VISIBLE
            return
        }

        chart.visibility = View.VISIBLE
        noDataText.visibility = View.GONE

        val categoryTotals = transactions
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val colors = if (isIncome) {
            listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#FF5722")
            )
        } else {
            listOf(
                Color.parseColor("#F44336"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#673AB7"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#795548"),
                Color.parseColor("#607D8B")
            )
        }

        val dataSet = PieDataSet(entries, if (isIncome) "Income Categories" else "Expense Categories").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(chart)
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        chart.apply {
            data = PieData(dataSet)
            highlightValues(null)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 