package com.example.finewise.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.finewise.databinding.FragmentEditTransactionBinding
import com.example.finewise.model.Transaction
import com.example.finewise.utils.PrefsUtils
import java.util.Date

class EditTransactionFragment : Fragment() {
    private var _binding: FragmentEditTransactionBinding? = null
    private val binding get() = _binding!!
    private val args: EditTransactionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup category spinner
        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // Load transaction data
        val transaction = args.transaction
        binding.etTitle.setText(transaction.title)
        binding.etAmount.setText(transaction.amount.toString())
        binding.spinnerCategory.setSelection(categories.indexOf(transaction.category))
        binding.rgType.check(if (transaction.isIncome) binding.rbIncome.id else binding.rbExpense.id)

        // Setup save button
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amountStr = binding.etAmount.text.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val isIncome = binding.rgType.checkedRadioButtonId == binding.rbIncome.id

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTransaction = Transaction(
                id = transaction.id,
                title = title,
                amount = amount,
                category = category,
                isIncome = isIncome,
                date = transaction.date
            )

            // Update transaction in storage
            val transactions = PrefsUtils.getTransactions(requireContext())
            val updatedList = transactions.map { 
                if (it.id == transaction.id) updatedTransaction else it 
            }
            PrefsUtils.saveTransactions(requireContext(), updatedList)

            Toast.makeText(context, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // Setup cancel button
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 