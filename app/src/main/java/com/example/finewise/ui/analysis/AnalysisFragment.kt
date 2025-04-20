package com.example.finewise.ui.analysis

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
        val summaries = transactions.groupBy { it.category }
            .map { (category, list) ->
                CategorySummary(category, list.sumOf { if (it.isIncome) 0.0 else it.amount })
            }.filter { it.total > 0 }

        binding.rvCategories.layoutManager = LinearLayoutManager(context)
        binding.rvCategories.adapter = CategoryAdapter(summaries)

        val entries = summaries.map { PieEntry(it.total.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "Spending by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}