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
import eu.nets.lab.smartpos.sdk.client.LegacyClient
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.client.PaymentManager
import eu.nets.lab.smartpos.sdk.payload.AuxString
import eu.nets.lab.smartpos.sdk.payload.paymentData
import eu.nets.lab.smartpos.sdk.utility.printer.PrinterBeta
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinter
import java.util.*

class SalesFragment : Fragment() {

    private val salesViewModel: SalesViewModel by viewModels()
    private var _binding: FragmentSalesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var paymentManager: PaymentManager

    @OptIn(PrinterBeta::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetsClient.get(this).use {
            // Handle result from Nets client
            this.paymentManager = it.paymentManager.register { result ->
                salesViewModel.setText(result.status.toString())
                salesViewModel.persistResult(result)

                // Print a receipt slip
                SlipPrinter.getInstance(result, requireContext(), false)?.printPaymentSlip()

                // Disable buttons - we can't send another sales request with the same uuid
                binding.payNetsClient.isEnabled = false
                binding.payLegacyClient.isEnabled = false
            }
        }
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

        var currency: String = "EUR"

        binding.currency.adapter = currencyAdapter
        binding.currency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currency = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currency = "EUR"
            }
        }
        binding.currency.setSelection(currencyAdapter.getPosition("EUR"))
        // endregion

        // Create payment data
        // region payment-data
        val data = paymentData {
            this.uuid = UUID.randomUUID()
            this.amount = binding.amount.text.toString().toLongOrNull() ?: 1000
            this.vat = binding.amount.text.toString().toLongOrNull() ?: 250
            this.currency = currency
            this.aux put "key" value "This is a test value"
            this.requestedMethod = null
        }
        // endregion

        // Set button click listeners
        // region button-listeners
        binding.payNetsClient.setOnClickListener {
            paymentManager.process(data)
        }

        binding.payLegacyClient.setOnClickListener {
            val intent = LegacyClient.paymentIntent(data)
            startActivityForResult(intent, 2)
        }
        // endregion

        val textSales: TextView = binding.textSales
        salesViewModel.text.observe(viewLifecycleOwner) {
            textSales.text = it
        }
        return root
    }

    @OptIn(PrinterBeta::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Receive result when using LegacyClient
        val result = LegacyClient.extractPaymentResult(data, resultCode)
        if ((result.aux["cause"] as? AuxString)?.value != "no response") {
            salesViewModel.setText(result.status.toString())
            salesViewModel.persistResult(result)

            // Print a receipt slip
            SlipPrinter.getInstance(result, requireContext(), false)?.printPaymentSlip()

            // Disable buttons - we can't send another sales request with the same uuid
            binding.payNetsClient.isEnabled = false
            binding.payLegacyClient.isEnabled = false
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}