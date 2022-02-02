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
    
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var cur: String

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        this.cur = sharedPreferences.getString("preference_currency", "EUR")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
