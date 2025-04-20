package com.example.finewise.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finewise.R
import com.example.finewise.adapter.TransactionAdapter
import com.example.finewise.databinding.FragmentHomeBinding
import com.example.finewise.model.Transaction
import com.example.finewise.utils.PrefsUtils
import java.util.Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private var editingTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load transactions
        transactions.addAll(PrefsUtils.getTransactions(requireContext()))
        adapter = TransactionAdapter(transactions, { transaction ->
            editingTransaction = transaction
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            binding.spinnerCategory.setSelection(
                resources.getStringArray(R.array.categories).indexOf(transaction.category)
            )
            binding.rgType.check(if (transaction.isIncome) R.id.rb_income else R.id.rb_expense)
            binding.btnAdd.text = "Update"
        }, { transaction ->
            transactions.remove(transaction)
            adapter.update(transactions)
            PrefsUtils.saveTransactions(requireContext(), transactions)
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
        })
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.adapter = adapter

        // Setup Spinner
        val categories = resources.getStringArray(R.array.categories)
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Add/Update Button
        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val isIncome = binding.rgType.checkedRadioButtonId == R.id.rb_income

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editingTransaction != null) {
                val index = transactions.indexOf(editingTransaction)
                transactions[index] = editingTransaction!!.copy(
                    title = title,
                    amount = amount,
                    category = category,
                    isIncome = isIncome
                )
                editingTransaction = null
                binding.btnAdd.text = "Add Transaction"
            } else {
                transactions.add(
                    Transaction(
                        id = transactions.size + 1,
                        title = title,
                        amount = amount,
                        category = category,
                        date = Date(),
                        isIncome = isIncome
                    )
                )
            }

            adapter.update(transactions)
            PrefsUtils.saveTransactions(requireContext(), transactions)
            binding.etTitle.text.clear()
            binding.etAmount.text.clear()
            binding.spinnerCategory.setSelection(0)
            binding.rgType.check(R.id.rb_income)
            Toast.makeText(context, "Transaction saved", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}