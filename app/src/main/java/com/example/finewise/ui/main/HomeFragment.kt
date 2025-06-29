package com.example.finewise.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finewise.R
import com.example.finewise.adapter.TransactionAdapter
import com.example.finewise.databinding.FragmentHomeBinding
import com.example.finewise.model.Transaction
import com.example.finewise.utils.FileUtils
import com.example.finewise.utils.PrefsUtils
import java.text.NumberFormat
import java.util.Currency
import java.util.Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private lateinit var currencyFormatter: NumberFormat

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with backup/restore
        } else {
            Toast.makeText(context, "Storage permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrencyFormatter()
        setupRecyclerView()
        loadTransactions()
        setupSpinner()
        setupButtons()
    }

    private fun setupCurrencyFormatter() {
        val currencyCode = PrefsUtils.getSelectedCurrency(requireContext())
        currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
        if (::adapter.isInitialized) {
            adapter.updateCurrencyFormatter(currencyFormatter)
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            transactions = transactions,
            onEdit = { transaction ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToEditTransaction(transaction)
                )
            },
            onDelete = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                transactions.remove(transaction)
                updateTransactionList()
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadTransactions() {
        val savedTransactions = PrefsUtils.getTransactions(requireContext())
        Log.d("HomeFragment", "Loaded transactions: ${savedTransactions.size}")
        transactions.clear()
        transactions.addAll(savedTransactions)
        updateTransactionList()
    }

    private fun updateTransactionList() {
        Log.d("HomeFragment", "Updating transaction list. Size: ${transactions.size}")
        adapter.notifyDataSetChanged()
        PrefsUtils.saveTransactions(requireContext(), transactions)
        
        // Update visibility of the empty state
        if (transactions.isEmpty()) {
            binding.tvNoTransactions.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.GONE
        } else {
            binding.tvNoTransactions.visibility = View.GONE
            binding.rvTransactions.visibility = View.VISIBLE
        }
    }

    private fun setupSpinner() {
        updateCategorySpinner(isIncome = true) // Default to income categories
        
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            val isIncome = checkedId == R.id.rb_income
            updateCategorySpinner(isIncome)
        }
    }

    private fun updateCategorySpinner(isIncome: Boolean) {
        val categories = resources.getStringArray(
            if (isIncome) R.array.income_categories else R.array.expense_categories
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        (binding.spinnerCategory as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setText("", false)
            hint = "Select category"
        }
    }

    private fun setupButtons() {
        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val category = (binding.spinnerCategory as? AutoCompleteTextView)?.text?.toString() ?: ""
            val isIncome = binding.rgType.checkedRadioButtonId == R.id.rb_income

            if (title.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTransaction = Transaction(
                id = System.currentTimeMillis().toInt(),
                title = title,
                amount = amount,
                category = category,
                date = Date(),
                isIncome = isIncome
            )
            transactions.add(newTransaction)
            updateTransactionList()
            clearInputFields()
            Toast.makeText(context, "Transaction added successfully", Toast.LENGTH_SHORT).show()
        }

        setupBackupRestoreButtons()
    }

    private fun clearInputFields() {
        binding.etTitle.text?.clear()
        binding.etAmount.text?.clear()
        (binding.spinnerCategory as? AutoCompleteTextView)?.setText("", false)
        binding.rgType.check(R.id.rb_income)
        updateCategorySpinner(isIncome = true)
    }

    private fun setupBackupRestoreButtons() {
        binding.btnBackup.setOnClickListener {
            if (checkStoragePermission()) {
                if (FileUtils.exportTransactions(requireContext(), transactions)) {
                    Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRestore.setOnClickListener {
            if (checkStoragePermission()) {
                val restoredTransactions = FileUtils.importTransactions(requireContext())
                if (restoredTransactions.isNotEmpty()) {
                    transactions.clear()
                    transactions.addAll(restoredTransactions)
                    updateTransactionList()
                    Toast.makeText(context, "Restore successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No backup found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnExportText.setOnClickListener {
            if (checkStoragePermission()) {
                val filePath = FileUtils.exportTransactionsAsText(requireContext(), transactions)
                if (filePath != null) {
                    Toast.makeText(
                        context,
                        "Transactions exported to Downloads folder",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupCurrencyFormatter()
        loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkStoragePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                true
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    context,
                    "Storage permission is required for backup/restore",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(permission)
                false
            }
            else -> {
                requestPermissionLauncher.launch(permission)
                false
            }
        }
    }
}