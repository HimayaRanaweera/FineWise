package com.example.finewise.ui.budget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finewise.databinding.FragmentBudgetBinding
import com.example.finewise.utils.NotificationUtils
import com.example.finewise.utils.PrefsUtils
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var currencyFormatter: NumberFormat

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNotificationPermission()
        setupCurrencySelector()

        // Load saved budget
        val sharedPrefs = requireContext().getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        val savedBudget = sharedPrefs.getFloat("monthly_budget", 0f)
        if (savedBudget > 0) {
            binding.budgetInput.setText(String.format("%.2f", savedBudget.toDouble()))
        }

        // Setup save button
        binding.saveBudgetButton.setOnClickListener {
            val budgetInput = binding.budgetInput.text.toString()
            if (budgetInput.isEmpty()) {
                Toast.makeText(context, "Please enter a budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val budget = budgetInput.toDoubleOrNull() ?: 0.0
            if (budget <= 0) {
                Toast.makeText(context, "Enter a valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save budget to SharedPreferences
            sharedPrefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
            updateBudgetStatus()
            Toast.makeText(context, "Budget saved successfully", Toast.LENGTH_SHORT).show()
        }

        // Initial update
        updateBudgetStatus()
    }

    private fun setupCurrencySelector() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "LKR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        binding.currencySelector.setAdapter(adapter)

        // Set current currency
        val currentCurrency = PrefsUtils.getSelectedCurrency(requireContext())
        binding.currencySelector.setText(currentCurrency, false)

        // Handle currency selection
        binding.currencySelector.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            PrefsUtils.saveCurrency(requireContext(), selectedCurrency)
            updateCurrencyFormatter(selectedCurrency)
            updateBudgetStatus()
        }

        // Initialize currency formatter
        updateCurrencyFormatter(currentCurrency)
    }

    private fun updateCurrencyFormatter(currencyCode: String) {
        currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
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
        } else {
            NotificationUtils.createNotificationChannel(requireContext())
        }
    }

    private fun updateBudgetStatus() {
        val transactions = PrefsUtils.getTransactions(requireContext())
        val budget = binding.budgetInput.text.toString().toDoubleOrNull() ?: 0.0

        // Calculate totals
        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val remaining = budget - totalExpenses
        val savings = totalIncome - totalExpenses

        // Update status texts
        binding.budgetStatus.text = "Budget: ${currencyFormatter.format(budget)}"
        binding.incomeStatus.text = "Total Income: ${currencyFormatter.format(totalIncome)}"
        binding.expenseStatus.text = "Total Expenses: ${currencyFormatter.format(totalExpenses)}"
        
        binding.remainingBudget.apply {
            text = "Remaining: ${currencyFormatter.format(remaining)}"
            setTextColor(if (remaining < 0) Color.RED else Color.parseColor("#4CAF50"))
        }

        binding.savingsStatus.apply {
            text = "Net Savings: ${currencyFormatter.format(savings)}"
            setTextColor(if (savings < 0) Color.RED else Color.parseColor("#4CAF50"))
        }

        // Show/hide warning and send notification if budget exceeded
        if (totalExpenses > budget && budget > 0) {
            binding.budgetWarning.visibility = View.VISIBLE
            NotificationUtils.showBudgetExceededNotification(requireContext(), budget, totalExpenses)
        } else {
            binding.budgetWarning.visibility = View.GONE
        }

        // Update progress bars
        val budgetProgress = if (budget > 0) ((totalExpenses / budget) * 100).toInt() else 0
        binding.budgetProgress.apply {
            progress = budgetProgress.coerceIn(0, 100)
            setIndicatorColor(if (budgetProgress > 100) Color.RED else Color.parseColor("#4CAF50"))
        }

        val savingsProgress = if (totalIncome > 0) ((savings / totalIncome) * 100).toInt() else 0
        binding.savingsProgress.apply {
            progress = savingsProgress.coerceIn(0, 100)
            setIndicatorColor(if (savingsProgress < 0) Color.RED else Color.parseColor("#4CAF50"))
        }
    }

    override fun onResume() {
        super.onResume()
        updateBudgetStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}