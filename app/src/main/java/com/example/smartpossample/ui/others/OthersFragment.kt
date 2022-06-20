package com.example.smartpossample.ui.others

import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartpossample.R
import com.example.smartpossample.databinding.FragmentOthersBinding
import eu.nets.lab.smartpos.sdk.client.*
import eu.nets.lab.smartpos.sdk.payload.*
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinterUtility
import java.util.*

class OthersFragment : Fragment() {

    private val othersViewModel: OthersViewModel by viewModels()
    private var _binding: FragmentOthersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var payload: ResultPayload? = null

    private lateinit var reversalManager: ReversalManager
    private lateinit var legacyReversalManager: LegacyReversalManager
    private lateinit var endOfDayManager: EndOfDayManager
    private lateinit var legacyEndOfDayManager: LegacyEndOfDayManager
    private lateinit var statusManager: StatusManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetsClient.get().use { client ->
            this.reversalManager = client.reversalManager(this).register { result ->
                othersViewModel.setText(result.status.toString())
                SlipPrinterUtility(requireContext()).let { printer ->
                    printer.printCustomerReceipt(
                        result,
                        false,
                        SlipPrinterUtility.SlipFlag.RECIPIENT_INFO
                    )

                    printer.free()
                }
            }
            this.legacyReversalManager =
                client.legacyReversalManager(this::startActivityForResult).register { result ->
                    othersViewModel.setText(result.status.toString())
                    SlipPrinterUtility(requireContext()).let { printer ->
                        printer.printCustomerReceipt(
                            result,
                            false,
                            SlipPrinterUtility.SlipFlag.RECIPIENT_INFO
                        )

                        printer.free()
                    }
                }
            this.endOfDayManager = client.endOfDayManager(this).register { result ->
                othersViewModel.setText("End of day result ${result.eodAux}")
            }
            this.legacyEndOfDayManager =
                client.legacyEndOfDayManager(this::startActivityForResult).register { result ->
                    othersViewModel.setText("End of day result ${result.eodAux}")
                }
            this.statusManager = client.statusManager(this).register { result ->
                // This is not included in the tutorial
                if (result is ReceiptSlipPayload) {
                    SlipPrinterUtility(requireContext()).let { printer ->
                        printer.printCustomerReceipt(
                            result,
                            true,
                            SlipPrinterUtility.SlipFlag.RECIPIENT_INFO,
                            SlipPrinterUtility.SlipFlag.REQUIRE_SIGNATURE_IF_AVAILABLE
                        )
                    }
                }

                val status = when (result) {
                    is PaymentResult -> {
                        result.status.toString()
                    }
                    is RefundResult -> {
                        result.status.toString()
                    }
                    is ReversalResult -> {
                        result.status.toString()
                    }
                    else -> "Not a result"
                }
                othersViewModel.setText("Query: ${result::class.simpleName} ($status)")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOthersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up payload from newest stored transaction
        // region reversal-payload
        othersViewModel.newest().observe(viewLifecycleOwner) {
            payload = when (it) {
                is PaymentResult -> {
                    binding.type.text = "Payment"
                    binding.totalAmount.text = (it.data.amount + it.data.vat).toString()
                    binding.currency.text = it.data.currency
                    binding.status.text = it.status.toString()
                    it
                }
                is RefundResult -> {
                    binding.type.text = "Refund"
                    binding.totalAmount.text = (it.data.amount + it.data.vat).toString()
                    binding.currency.text = it.data.currency
                    binding.status.text = it.status.toString()
                    it
                }
                else -> {
                    othersViewModel.setText("No transactions stored")
                    null
                }
            }
        }
        // endregion

        // Set up end of day payload
        // region end-of-day-payload
        val eodData = ParcelUuid(UUID.randomUUID())
        // endregion

        // Set up query
        // region query
        val query = transactionStatusRequest {
            type = TransactionStatusRequest.TransactionStatusRequestType.PAYMENT_REFUND
            info = TransactionStatusRequest.TransactionStatusRequestInfo.RESULT
            pingCardTransaction = false
            uuid = null
        }
        // endregion

        // Set button click listeners
        // region button-listeners
        binding.reverseNetsClient.setOnClickListener {
            payload?.let { p ->
                reversalManager.process(p)
            }
        }

        binding.reverseLegacyClient.setOnClickListener {
            payload?.let { p ->
                legacyReversalManager.process(p)
            }
        }

        binding.endOfDayNetsClient.setOnClickListener {
            endOfDayManager.process(eodData)
        }

        binding.endOfDayLegacyClient.setOnClickListener {
            legacyEndOfDayManager.process(eodData)
        }

        binding.statusNetsClient.setOnClickListener {
            statusManager.process(query)
        }
        // endregion

        val textView: TextView = binding.textOthers
        othersViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // This is not included in the tutorial
        binding.navigateToUtility.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_others_to_utilityFragment)
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        legacyReversalManager.handleResult(requestCode, resultCode, data)
        legacyEndOfDayManager.handleResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}