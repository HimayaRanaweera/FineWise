package com.example.finewise.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finewise.adapter.CategoryAdapter
import com.example.finewise.adapter.CategorySummary
import com.example.finewise.databinding.FragmentAnalysisBinding
import com.example.finewise.utils.PrefsUtils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transactions = PrefsUtils.getTransactions(requireContext())
        
        // Calculate total income
        val totalIncome = transactions
            .filter { it.isIncome }
            .sumOf { it.amount }
        
        // Calculate category summaries for expenses
        val expenseSummaries = transactions
            .filter { !it.isIncome }
            .groupBy { it.category }
            .map { (category, list) ->
                CategorySummary(
                    category = category,
                    total = list.sumOf { it.amount },
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        // Calculate total expenses
        val totalExpenses = expenseSummaries.sumOf { it.total }
        
        // Calculate net balance
        val netBalance = totalIncome - totalExpenses

        // Update UI with financial data
        updateFinancialData(totalIncome, totalExpenses, netBalance)

        // Setup RecyclerView with fixed height
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CategoryAdapter(expenseSummaries)
            isNestedScrollingEnabled = true
        }

        // Setup PieChart
        setupPieChart(expenseSummaries, totalExpenses)
    }

    private fun updateFinancialData(totalIncome: Double, totalExpenses: Double, netBalance: Double) {
        binding.tvTotalIncome.text = currencyFormatter.format(totalIncome)
        binding.tvTotalExpenses.text = currencyFormatter.format(totalExpenses)
        binding.tvBalance.apply {
            text = currencyFormatter.format(netBalance)
            setTextColor(if (netBalance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        }
    }

    private fun setupPieChart(summaries: List<CategorySummary>, totalExpenses: Double) {
        with(binding.pieChart) {
            // Basic setup
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Total Expenses\n${currencyFormatter.format(totalExpenses)}"
            setCenterTextSize(14f)
            
            // Enable rotation and animation
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            
            // Legend setup
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 5f
                textSize = 12f
                isWordWrapEnabled = true
                maxSizePercent = 0.5f
            }
            
            // Create entries for non-zero values
            val entries = summaries
                .filter { it.total > 0 }
                .map { PieEntry(it.total.toFloat(), it.category) }

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        Color.parseColor("#2196F3"), // Blue
                        Color.parseColor("#4CAF50"), // Green
                        Color.parseColor("#FFC107"), // Yellow
                        Color.parseColor("#FF5722"), // Deep Orange
                        Color.parseColor("#9C27B0")  // Purple
                    )
                    sliceSpace = 3f
                    selectionShift = 5f
                    valueLinePart1OffsetPercentage = 80f
                    valueLinePart1Length = 0.2f
                    valueLinePart2Length = 0.4f
                    xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    valueLineColor = Color.BLACK
                }

                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter(binding.pieChart))
                    setValueTextSize(11f)
                    setValueTextColor(Color.BLACK)
                }
            }
            
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}