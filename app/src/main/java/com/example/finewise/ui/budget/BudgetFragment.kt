package com.example.finewise.ui.budget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finewise.databinding.FragmentBudgetBinding
import com.example.finewise.utils.NotificationUtils
import com.example.finewise.utils.PrefsUtils
import java.text.NumberFormat
import android.view.View.VISIBLE
import android.view.View.GONE
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private var budget: Double = 0.0
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationUtils.createNotificationChannel(requireContext())
        } else {
            Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNotificationPermission()
        setupBudgetInput()
        updateAllStats()
    }

    private fun setupNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationUtils.createNotificationChannel(requireContext())
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(context, "Notifications are required for budget alerts", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupBudgetInput() {
        budget = PrefsUtils.getBudget(requireContext())
        if (budget > 0) {
            binding.budgetInput.setText(budget.toString())
        }

        binding.saveBudgetButton.setOnClickListener {
            val budgetStr = binding.budgetInput.text.toString()
            if (budgetStr.isEmpty()) {
                Toast.makeText(context, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            budget = budgetStr.toDoubleOrNull() ?: 0.0
            if (budget <= 0) {
                Toast.makeText(context, "Please enter a valid budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PrefsUtils.saveBudget(requireContext(), budget)
            updateAllStats()
            binding.budgetInput.text?.clear()
            Toast.makeText(context, "Budget updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAllStats() {
        val transactions = PrefsUtils.getTransactions(requireContext())
        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val remaining = budget - expenses
        val savings = income - expenses

        // Update Income Card
        binding.incomeStatus.text = currencyFormatter.format(income)

        // Update Expenses Card
        binding.expenseStatus.text = currencyFormatter.format(expenses)

        // Update Budget Status Card
        binding.budgetStatus.text = "Budget: ${currencyFormatter.format(budget)}"
        binding.remainingBudget.text = "Remaining: ${currencyFormatter.format(remaining)}"
        
        // Update Budget Progress
        if (budget > 0) {
            val progress = ((expenses / budget) * 100).toInt().coerceIn(0, 100)
            binding.budgetProgress.progress = progress
        } else {
            binding.budgetProgress.progress = 0
        }

        // Show/Hide Budget Warning
        binding.budgetWarning.visibility = if (remaining < 0) VISIBLE else GONE
        if (remaining < 0) {
            NotificationUtils.showBudgetExceededNotification(requireContext())
        }

        // Update Savings Card
        binding.savingsStatus.text = currencyFormatter.format(savings)
        
        // Update Savings Progress (assuming 20% of income as target savings)
        val targetSavings = income * 0.2
        if (income > 0) {
            val savingsProgress = ((savings / targetSavings) * 100).toInt().coerceIn(0, 100)
            binding.savingsProgress.progress = savingsProgress
        } else {
            binding.savingsProgress.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}