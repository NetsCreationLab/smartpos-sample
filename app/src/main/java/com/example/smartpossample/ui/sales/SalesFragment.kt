package com.example.smartpossample.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.smartpossample.R
import com.example.smartpossample.databinding.FragmentSalesBinding

class SalesFragment : Fragment() {

    private val salesViewModel: SalesViewModel by viewModels()
    private var _binding: FragmentSalesBinding? = null

    private var cur = "EUR"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup currency spinner
        // region currency-spinner
        val currencyAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currency_entries,
            android.R.layout.simple_spinner_item,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.currency.adapter = currencyAdapter
        binding.currency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                cur = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                cur = "EUR"
            }
        }
        binding.currency.setSelection(currencyAdapter.getPosition("EUR"))
        // endregion

        val textSales: TextView = binding.textSales
        salesViewModel.text.observe(viewLifecycleOwner) {
            textSales.text = it
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}