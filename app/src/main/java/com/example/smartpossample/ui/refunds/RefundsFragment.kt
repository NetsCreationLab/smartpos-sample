package com.example.smartpossample.ui.refunds

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.example.smartpossample.databinding.FragmentRefundsBinding
import eu.nets.lab.smartpos.sdk.client.LegacyRefundManager
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.client.RefundManager
import eu.nets.lab.smartpos.sdk.payload.*
import eu.nets.lab.smartpos.sdk.utility.printer.ErrorSlipPrinter
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinter
import java.util.*

class RefundsFragment : Fragment() {

    private val refundsViewModel: RefundsViewModel by viewModels()
    private var _binding: FragmentRefundsBinding? = null

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var cur: String
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
    private lateinit var legacyRefundManager: LegacyRefundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        this.cur = sharedPreferences.getString("preference_currency", "EUR")!!

        NetsClient.get().use { client ->
            this.refundManager = client.refundManager(this).register { result ->
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
            }
            this.legacyRefundManager = client.legacyRefundManager(this::startActivityForResult).register { result ->
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

        // Setup method spinner
        // region method-spinner
        NetsClient.get().use { client ->
            client.advancedAdminManager(requireContext()).register { result ->
                val methodsList = result.methodResultOrNull() ?: listOf(TargetMethod.CARD)

                val methodsAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    methodsList.map { it.toString() }.toTypedArray(),
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                binding.method.adapter = methodsAdapter
                binding.method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        chosenMethod = TargetMethod.valueOf(parent.getItemAtPosition(position).toString())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        chosenMethod = TargetMethod.CARD
                    }
                }
                binding.method.setSelection(methodsAdapter.getPosition(TargetMethod.CARD.toString()))
            }.process(AdminRequest.methodRequest)
        }
        // endregion

        // Set button click listeners
        // region button-listeners
        binding.refundNetsClient.setOnClickListener {
            refundManager.process(data)
        }

        binding.refundLegacyClient.setOnClickListener {
            legacyRefundManager.process(data)
        }
        // endregion

        val textRefunds: TextView = binding.textRefunds
        refundsViewModel.text.observe(viewLifecycleOwner) {
            textRefunds.text = it
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        legacyRefundManager.handleResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}