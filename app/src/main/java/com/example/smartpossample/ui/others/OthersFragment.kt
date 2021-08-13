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
import com.example.smartpossample.databinding.FragmentOthersBinding
import eu.nets.lab.smartpos.sdk.client.*
import eu.nets.lab.smartpos.sdk.payload.*
import java.util.*

class OthersFragment : Fragment() {

    private val othersViewModel: OthersViewModel by viewModels()
    private var _binding: FragmentOthersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var payload: ResultPayload? = null

    private lateinit var reversalManager: ReversalManager
    private lateinit var endOfDayManager: EndOfDayManager
    private lateinit var statusManager: StatusManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetsClient.get(this).use {
            this.reversalManager = it.reversalManager.register { result ->
                othersViewModel.setText(result.status.toString())
            }
            this.endOfDayManager = it.endOfDayManager.register { result ->
                othersViewModel.setText("End of day result ${result.eodAux}")
            }
            this.statusManager = it.statusManager.register { result ->
                val status = when (result) {
                    is PaymentResult -> result.status.toString()
                    is RefundResult -> result.status.toString()
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
        othersViewModel.newest.observe(viewLifecycleOwner) {
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
                    binding.totalAmount.text = it.data.totalAmount.toString()
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
            type = TransactionStatusRequest.TransactionStatusRequestType.BOTH
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
                val intent = LegacyClient.reversalIntent(p)
                startActivityForResult(intent, 2)
            }
        }

        binding.endOfDayNetsClient.setOnClickListener {
            endOfDayManager.process(eodData)
        }

        binding.endOfDayLegacyClient.setOnClickListener {
            val intent = LegacyClient.eodIntent(eodData)
            startActivityForResult(intent, 3)
        }

        binding.statusNetsClient.setOnClickListener {
            statusManager.process(query)
        }

        binding.statusLegacyClient.setOnClickListener {
            val intent = LegacyClient.statusIntent(query)
            startActivityForResult(intent, 4)
        }
        // endregion

        val textView: TextView = binding.textOthers
        othersViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Receive result when using LegacyClient
        when (requestCode) {
            2 -> {
                val result = LegacyClient.extractReversalResult(data, 2)
                if ((result.aux["cause"] as? AuxString)?.value != "no response") {
                    othersViewModel.setText(result.status.toString())
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
            3 -> {
                val result = LegacyClient.extractEodResult(data, 2)
                if ((result.eodAux[TargetMethod.CRASH]?.get("cause") as? AuxString)?.value != "no response") {
                    othersViewModel.setText("End of day result ${result.eodAux}")
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
            4 -> {
                val result = LegacyClient.extractStatusResult(data, 2)
                if (result !is TransactionPayload.NoPayload) {
                    val status = when (result) {
                        is PaymentResult -> result.status.toString()
                        is RefundResult -> result.status.toString()
                        else -> "Not a result"
                    }
                    othersViewModel.setText("Query: ${result::class.simpleName} ($status)")
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}