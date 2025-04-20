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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.util.Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadTransactions()
        setupSpinner()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            transactions,
            onEdit = { transaction ->
                // Navigate to edit fragment
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToEditTransaction(transaction)
                )
            },
            onDelete = { transaction ->
                transactions.remove(transaction)
                updateTransactionList()
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }
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
        val categories = resources.getStringArray(R.array.categories)
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply { 
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupButtons() {
        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val isIncome = binding.rgType.checkedRadioButtonId == R.id.rb_income

            if (title.isEmpty() || amountStr.isEmpty()) {
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
            transactions.add(newTransaction) // Add to the end of the list
            Log.d("HomeFragment", "Added new transaction. New size: ${transactions.size}")

            updateTransactionList()
            clearInputFields()
            Toast.makeText(context, "Transaction added successfully", Toast.LENGTH_SHORT).show()
        }

        setupBackupRestoreButtons()
    }

    private fun clearInputFields() {
        binding.etTitle.text?.clear()
        binding.etAmount.text?.clear()
        binding.spinnerCategory.setSelection(0)
        binding.rgType.check(R.id.rb_income)
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
    }

    override fun onResume() {
        super.onResume()
        // Reload transactions when returning from edit screen
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