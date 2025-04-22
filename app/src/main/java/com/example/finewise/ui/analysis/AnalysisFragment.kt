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
import com.example.finewise.model.Transaction
import com.example.finewise.utils.PrefsUtils
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Currency

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var currencyFormatter: NumberFormat
    private lateinit var categoryAdapter: CategoryAdapter

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
        setupCurrencyFormatter()
        setupRecyclerView()
        updateUI()
    }

    private fun setupCurrencyFormatter() {
        val currencyCode = PrefsUtils.getSelectedCurrency(requireContext())
        currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
        if (::categoryAdapter.isInitialized) {
            categoryAdapter.updateCurrencyFormatter(currencyFormatter)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList(), currencyFormatter)
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }
    }

    private fun updateUI() {
        val transactions = PrefsUtils.getTransactions(requireContext())
        val incomeTransactions = transactions.filter { it.isIncome }
        val expenseTransactions = transactions.filter { !it.isIncome }

        // Update financial summary
        val totalIncome = incomeTransactions.sumOf { it.amount }
        val totalExpenses = expenseTransactions.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        binding.tvTotalIncome.text = currencyFormatter.format(totalIncome)
        binding.tvTotalExpenses.text = currencyFormatter.format(totalExpenses)
        binding.tvBalance.text = currencyFormatter.format(balance)

        // Update income chart
        if (incomeTransactions.isNotEmpty()) {
            binding.tvNoIncome.visibility = View.GONE
            binding.incomeChart.visibility = View.VISIBLE
            setupChart(incomeTransactions, binding.incomeChart, true)
        } else {
            binding.tvNoIncome.visibility = View.VISIBLE
            binding.incomeChart.visibility = View.GONE
        }

        // Update expense chart
        if (expenseTransactions.isNotEmpty()) {
            binding.tvNoExpense.visibility = View.GONE
            binding.expenseChart.visibility = View.VISIBLE
            setupChart(expenseTransactions, binding.expenseChart, false)
        } else {
            binding.tvNoExpense.visibility = View.VISIBLE
            binding.expenseChart.visibility = View.GONE
        }

        // Update categories RecyclerView with expense summaries only
        val categorySummaries = getCategorySummaries(expenseTransactions)
        categoryAdapter = CategoryAdapter(categorySummaries, currencyFormatter)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupChart(transactions: List<Transaction>, chart: com.github.mikephil.charting.charts.PieChart, isIncome: Boolean) {
        val categoryMap = transactions.groupBy { it.category }
        val entries = categoryMap.map { (category, categoryTransactions) ->
            val total = categoryTransactions.sumOf { it.amount }
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, if (isIncome) "Income" else "Expenses").apply {
            colors = getChartColors(entries.size)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return currencyFormatter.format(value.toDouble())
                }
            }
            valueTextSize = 12f
        }

        chart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
            invalidate()
        }
    }

    private fun getCategorySummaries(transactions: List<Transaction>): List<CategorySummary> {
        return transactions
            .groupBy { it.category }
            .map { (category, categoryTransactions) ->
                CategorySummary(
                    category = category,
                    total = categoryTransactions.sumOf { it.amount },
                    count = categoryTransactions.size
                )
            }
            .sortedByDescending { it.total }
    }

    private fun getChartColors(size: Int): List<Int> {
        val colors = mutableListOf<Int>()
        for (i in 0 until size) {
            colors.add(Color.rgb(
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt()
            ))
        }
        return colors
    }

    override fun onResume() {
        super.onResume()
        setupCurrencyFormatter()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}