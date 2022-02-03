package com.example.smartpossample.ui.sales

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
import com.example.smartpossample.R
import com.example.smartpossample.databinding.FragmentSalesBinding
import eu.nets.lab.smartpos.sdk.client.LegacyClient
import eu.nets.lab.smartpos.sdk.client.LegacyPaymentManager
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.client.PaymentManager
import eu.nets.lab.smartpos.sdk.payload.AuxString
import eu.nets.lab.smartpos.sdk.payload.PaymentData
import eu.nets.lab.smartpos.sdk.payload.paymentData
import eu.nets.lab.smartpos.sdk.utility.printer.PrinterBeta
import eu.nets.lab.smartpos.sdk.utility.printer.SlipPrinter
import java.util.*

class SalesFragment : Fragment() {

    private val salesViewModel: SalesViewModel by viewModels()
    private var _binding: FragmentSalesBinding? = null

    private lateinit var sharedPreferences: SharedPreferences
    private val observer: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "preference_currency") {
                this@SalesFragment.cur = sharedPreferences.getString(key, "EUR")!!
            }
        }

    private lateinit var cur: String

    // Create payment data
    // region payment-data
    private val data: PaymentData
        get() = data()

    private fun data(): PaymentData = paymentData {
        this.uuid = UUID.randomUUID()
        this.amount = binding.amount.text.toString().toLongOrNull() ?: 1000
        this.vat = binding.vat.text.toString().toLongOrNull() ?: 250
        this.currency = cur
        this.aux put "key" value "This is a test value"
        this.requestedMethod = null
    }
    // endregion

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var paymentManager: PaymentManager
    private lateinit var legacyPaymentManager: LegacyPaymentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        this.cur = sharedPreferences.getString("preference_currency", "EUR")!!
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(observer)

        NetsClient.get().use { client ->
            // Handle result from Nets client
            this.paymentManager = client.paymentManager(this).register { result ->
                salesViewModel.setText(result.status.toString())
                salesViewModel.persistResult(result)

                // Print a receipt slip
                // Slightly changed from tutorial
                SlipPrinter.getInstance(result, requireContext(), false)?.let { printer ->
                    printer.printPaymentSlip()
                    // Printer.free() will "print" some empty receipt to allow tearing off without
                    // pulling first
                    printer.free()
                }
            }
            this.legacyPaymentManager = client.legacyPaymentManager(this::startActivityForResult).register { result ->
                salesViewModel.setText(result.status.toString())
                salesViewModel.persistResult(result)

                // Print a receipt slip
                // Slightly changed from tutorial
                SlipPrinter.getInstance(result, requireContext(), false)?.let { printer ->
                    printer.printPaymentSlip()
                    // Printer.free() will "print" some empty receipt to allow tearing off without
                    // pulling first
                    printer.free()
                }
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

        // Set button click listeners
        // region button-listeners
        binding.payNetsClient.setOnClickListener {
            paymentManager.process(data)
        }

        binding.payLegacyClient.setOnClickListener {
            legacyPaymentManager.process(data)
        }
        // endregion

        val textSales: TextView = binding.textSales
        salesViewModel.text.observe(viewLifecycleOwner) {
            textSales.text = it
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        legacyPaymentManager.handleResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(observer)
    }
}