package com.example.finewise.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finewise.adapter.CategoryAdapter
import com.example.finewise.adapter.CategorySummary
import com.example.finewise.databinding.FragmentAnalysisBinding
import com.example.finewise.utils.PrefsUtils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
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
    private lateinit var categoryAdapter: CategoryAdapter

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
        
        // Calculate income summaries
        val incomeSummaries = transactions
            .filter { it.isIncome }
            .groupBy { it.category }
            .map { (category, list) ->
                CategorySummary(
                    category = category,
                    total = list.sumOf { it.amount },
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        // Calculate expense summaries
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

        // Calculate totals
        val totalIncome = incomeSummaries.sumOf { it.total }
        val totalExpenses = expenseSummaries.sumOf { it.total }
        val netBalance = totalIncome - totalExpenses

        // Update UI with financial data
        updateFinancialData(totalIncome, totalExpenses, netBalance)

        // Setup RecyclerView
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CategoryAdapter(expenseSummaries)
            isNestedScrollingEnabled = false
        }

        // Setup charts and show/hide no data messages
        setupChart(binding.incomeChart, incomeSummaries, totalIncome, "Total Income", binding.tvNoIncome)
        setupChart(binding.expenseChart, expenseSummaries, totalExpenses, "Total Expenses", binding.tvNoExpense)
    }

    private fun updateFinancialData(totalIncome: Double, totalExpenses: Double, netBalance: Double) {
        binding.tvTotalIncome.text = currencyFormatter.format(totalIncome)
        binding.tvTotalExpenses.text = currencyFormatter.format(totalExpenses)
        binding.tvBalance.apply {
            text = currencyFormatter.format(netBalance)
            setTextColor(if (netBalance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        }
    }

    private fun setupChart(
        chart: PieChart,
        summaries: List<CategorySummary>,
        total: Double,
        centerText: String,
        noDataText: TextView
    ) {
        // Show/hide no data message
        noDataText.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
        
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            this.centerText = "$centerText\n${currencyFormatter.format(total)}"
            setCenterTextSize(14f)
            
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            
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
            
            val entries = summaries
                .filter { it.total > 0 }
                .map { PieEntry(it.total.toFloat(), it.category) }

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#4CAF50"),
                        Color.parseColor("#FFC107"),
                        Color.parseColor("#FF5722"),
                        Color.parseColor("#9C27B0")
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

                val pieData = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter())
                    setValueTextSize(11f)
                    setValueTextColor(Color.BLACK)
                }
                
                setUsePercentValues(true)
                data = pieData
            }
            
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}