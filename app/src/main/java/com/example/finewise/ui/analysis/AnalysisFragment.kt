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

class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

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
        
        // Calculate category summaries for expenses
        val expenseSummaries = transactions
            .filter { !it.isIncome } // Only expenses
            .groupBy { it.category }
            .map { (category, list) ->
                CategorySummary(
                    category = category,
                    total = list.sumOf { it.amount },
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        // Setup RecyclerView
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CategoryAdapter(expenseSummaries)
        }

        // Setup PieChart
        setupPieChart(expenseSummaries)
    }

    private fun setupPieChart(summaries: List<CategorySummary>) {
        with(binding.pieChart) {
            // Basic setup
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Expenses by Category"
            
            // Enable rotation and animation
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            
            // Legend setup
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }
            
            // Create entries for non-zero values
            val entries = summaries
                .filter { it.total > 0 }
                .map { PieEntry(it.total.toFloat(), it.category) }

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "Categories").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()
                    sliceSpace = 3f
                    selectionShift = 5f
                }

                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter(binding.pieChart))
                    setValueTextSize(11f)
                    setValueTextColor(Color.WHITE)
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