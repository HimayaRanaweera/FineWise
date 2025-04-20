package com.example.finewise.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finewise.databinding.FragmentBudgetBinding
import com.example.finewise.utils.NotificationUtils
import com.example.finewise.utils.PrefsUtils

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private var budget: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NotificationUtils.createNotificationChannel(requireContext())
        budget = PrefsUtils.getBudget(requireContext())
        updateStatus()

        binding.btnSetBudget.setOnClickListener {
            val budgetStr = binding.etBudget.text.toString()
            if (budgetStr.isEmpty()) {
                Toast.makeText(context, "Enter budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            budget = budgetStr.toDoubleOrNull() ?: 0.0
            if (budget <= 0) {
                Toast.makeText(context, "Enter valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PrefsUtils.saveBudget(requireContext(), budget)
            updateStatus()
            binding.etBudget.text.clear()
        }
    }

    private fun updateStatus() {
        val transactions = PrefsUtils.getTransactions(requireContext())
        val spent = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val remaining = budget - spent
        binding.tvBudgetStatus.text = "Budget: $$budget\nSpent: $$spent\nRemaining: $$remaining"
        if (remaining < 0) {
            NotificationUtils.showBudgetExceededNotification(requireContext())
            Toast.makeText(context, "Budget exceeded!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}