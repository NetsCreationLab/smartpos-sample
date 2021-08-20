package com.example.smartpossample.ui.refunds

import android.app.AlertDialog
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
import com.example.smartpossample.databinding.FragmentRefundsBinding
import eu.nets.lab.smartpos.sdk.client.LegacyClient
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.client.RefundManager
import eu.nets.lab.smartpos.sdk.payload.AuxString
import eu.nets.lab.smartpos.sdk.payload.RefundData
import eu.nets.lab.smartpos.sdk.payload.TargetMethod
import eu.nets.lab.smartpos.sdk.payload.refundData
import eu.nets.lab.smartpos.sdk.utility.printer.ErrorSlipPrinter
import eu.nets.lab.smartpos.sdk.utility.printer.PrinterBeta
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinter
import java.util.*

class RefundsFragment : Fragment() {

    private val refundsViewModel: RefundsViewModel by viewModels()
    private var _binding: FragmentRefundsBinding? = null

    private var cur = "EUR"
    private var chosenMethod = TargetMethod.CARD

    // Create refund data
    // region refund-data
    private val data: RefundData
        get() = data()

    private fun data(): RefundData {
            val amount = (binding.amount.text.toString().toLongOrNull() ?: 1000)
            val vat = (binding.vat.text.toString().toLongOrNull() ?: 250)
            return refundData {
                this.uuid = UUID.randomUUID()
                this.totalAmount = amount + vat
                this.currency = cur
                this.method = chosenMethod
                // Some payment methods require split VAT and amount for refunds too
                this.aux put "vatPaid" value vat
                this.aux put "amountPaid" value amount
            }
        }
    // endregion

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
                    // This is changed slightly compared to the tutorial
                    // We print both slips, but we put one behind an alert dialogue, so the
                    // cashier has time to rip off the first one
                    printer.printMerchantRefundSlip(true)
                    // Printer.free() will "print" some empty receipt to allow tearing off without
                    // pulling first
                    printer.free()
                    // Only show dialogue if this is not an ErrorSlipPrinter
                    if (printer !is ErrorSlipPrinter) {
                        AlertDialog
                            .Builder(requireActivity())
                            .setTitle("Print Customer Copy")
                            .setMessage("Do you want to print a copy for the customer?")
                            .setPositiveButton("Yes") { _, _ ->
                                printer.printCustomerRefundSlip()
                                printer.free()
                            }
                            .setNegativeButton("No") { dialogue, _ ->
                                dialogue.dismiss()
                            }
                            .create()
                            .show()
                    }
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

        // Setup method spinner
        // region method-spinner
        val methodsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.method_entries,
            android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.method.adapter = methodsAdapter
        binding.method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                chosenMethod = when (parent.getItemAtPosition(position).toString()) {
                    "SWISH" -> TargetMethod.SWISH
                    "INVOICE" -> TargetMethod.NETS_INVOICE
                    "EASY" -> TargetMethod.EASY
                    "SIMULATION" -> TargetMethod.SIMULATION
                    else -> TargetMethod.CARD
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                chosenMethod = TargetMethod.CARD
            }
        }
        binding.method.setSelection(methodsAdapter.getPosition("CARD"))
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