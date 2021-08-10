package com.example.smartpossample.ui.refunds

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
import androidx.lifecycle.Observer
import com.example.smartpossample.R
import com.example.smartpossample.databinding.FragmentRefundsBinding
import eu.nets.lab.smartpos.sdk.client.LegacyClient
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.client.RefundManager
import eu.nets.lab.smartpos.sdk.payload.AuxString
import eu.nets.lab.smartpos.sdk.payload.TargetMethod
import eu.nets.lab.smartpos.sdk.payload.refundData
import eu.nets.lab.smartpos.sdk.payload.toAux
import eu.nets.lab.smartpos.sdk.utility.printer.PrinterBeta
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinter
import java.util.*

class RefundsFragment : Fragment() {

    private val refundsViewModel: RefundsViewModel by viewModels()
    private var _binding: FragmentRefundsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var refundManager: RefundManager

    @OptIn(PrinterBeta::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetsClient.get(this).use {
            this.refundManager = it.refundManager.register { result ->
                // Handle result from Nets client
                refundsViewModel.setText(result.status.toString())
                refundsViewModel.persistResult(result)

                // Print receipt slips
                SlipPrinter.getInstance(result, requireContext(), false)?.let { printer ->
                    printer.printCustomerRefundSlip()
                    printer.getMerchantRefundSlip(true)
                }

                // Disable buttons - we can't send another refund request with the same uuid
                binding.refundNetsClient.isEnabled = false
                binding.refundLegacyClient.isEnabled = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefundsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup currency spinner
        // region currency-spinner
        val currencyAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currency_entries,
            android.R.layout.simple_spinner_item,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        var currency = "EUR"

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

        // Setup method spinner
        // region method-spinner
        val methodsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.method_entries,
            android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        var method = TargetMethod.CARD

        binding.method.adapter = methodsAdapter
        binding.method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                method = when (parent.getItemAtPosition(position).toString()) {
                    "SWISH" -> TargetMethod.SWISH
                    "INVOICE" -> TargetMethod.NETS_INVOICE
                    "EASY" -> TargetMethod.EASY
                    "SIMULATION" -> TargetMethod.SIMULATION
                    else -> TargetMethod.CARD
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                method = TargetMethod.CARD
            }
        }
        binding.method.setSelection(methodsAdapter.getPosition("CARD"))
        // endregion

        // Create refund data
        // region refund-data
        val amount = (binding.amount.text.toString().toLongOrNull() ?: 1000)
        val vat = (binding.amount.text.toString().toLongOrNull() ?: 250)
        val data = refundData {
            this.uuid = UUID.randomUUID()
            this.totalAmount = amount + vat
            this.currency = currency
            this.method = method
            // Some payment methods require split VAT and amount for refunds too
            this.aux put "VAT_PAID" value vat
            this.aux put "AMOUNT_PAID" value amount
        }
        // endregion

        // Set button click listeners
        // region button-listeners
        binding.refundNetsClient.setOnClickListener {
            refundManager.process(data)
        }

        binding.refundLegacyClient.setOnClickListener {
            val intent = LegacyClient.refundIntent(data)
            startActivityForResult(intent, 2)
        }
        // endregion

        val textRefunds: TextView = binding.textRefunds
        refundsViewModel.text.observe(viewLifecycleOwner) {
            textRefunds.text = it
        }
        return root
    }

    @OptIn(PrinterBeta::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Receive result when using LegacyClient
        val result = LegacyClient.extractRefundResult(data, resultCode)
        if ((result.aux["cause"] as? AuxString)?.value != "no response") {
            refundsViewModel.setText(result.status.toString())
            refundsViewModel.persistResult(result)

            // Print receipt slips
            SlipPrinter.getInstance(result, requireContext(), false)?.let { printer ->
                printer.printCustomerRefundSlip()
                printer.getMerchantRefundSlip(true)
            }

            // Disable buttons - we can't send another refund request with the same uuid
            binding.refundNetsClient.isEnabled = false
            binding.refundLegacyClient.isEnabled = false
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}