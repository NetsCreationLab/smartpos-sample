package com.example.smartpossample.ui.utility

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.smartpossample.R
import com.example.smartpossample.databinding.FragmentUtilityBinding
import com.example.smartpossample.printer.SamplePrinter
import eu.nets.lab.smartpos.sdk.utility.Log
import eu.nets.lab.smartpos.sdk.utility.printer.Printer
import eu.nets.lab.smartpos.sdk.utility.scanner.s1f2.*
import kotlin.contracts.ExperimentalContracts

/**
 * This fragment is not covered by the tutorial, but it provides examples
 * for using the scanner utility (only available on certain S1F2 variants)
 * and the printer utility (using the printer.SamplePrinter class)
 */
class UtilityFragment : Fragment() {
    private var _binding: FragmentUtilityBinding? = null

    private var printerWordWrap: Int = Printer.DEFAULT
    private var printerStyling: Int = Printer.NORMAL
    private var printerFontSize: Int = Printer.FONT_SIZE_REGULAR
    private var printerFont: Printer.Font = Printer.Font.SANS_SERIF

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val scanner = ScannerUtility.instance

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUtilityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val samplePrinter = SamplePrinter(requireActivity())

        binding.printerPrintThetragedy.setOnClickListener {
            if ((printerWordWrap and Printer.CUT_OFF) == 0 && (printerWordWrap and Printer.IGNORE) == 0) {
                AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.long_text))
                    .setMessage(getString(R.string.long_text_expl))
                    .setPositiveButton(getString(R.string.print)) { _, _ ->
                        samplePrinter.printTheTragedy(
                            printerFontSize,
                            printerWordWrap,
                            printerStyling,
                            printerFont
                        )
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialogue, _ ->
                        dialogue.dismiss()
                    }
                    .create()
                    .show()
            } else {
                samplePrinter.printTheTragedy(
                    printerFontSize,
                    printerWordWrap,
                    printerStyling,
                    printerFont
                )
            }
        }
        binding.printerPrintShorttest.setOnClickListener {
            samplePrinter.printShort(printerFontSize, printerWordWrap, printerStyling, printerFont)
        }
        binding.printerPrintLongtest.setOnClickListener {
            samplePrinter.printLong(printerFontSize, printerWordWrap, printerStyling, printerFont)
        }
        binding.printerPrintReceipt.setOnClickListener {
            samplePrinter.jokeyTestReceipt(false)
        }
        binding.printerPrintReceiptexplained.setOnClickListener {
            samplePrinter.jokeyTestReceipt(true)
        }

        // Fixed by Tor Niklas Strøm from Microlog AS (www.microlog.no)
        binding.printerWordwrapCutoffellipsis.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapPrioritiseleft.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapReversegravity.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapSplitleftright.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapWrapindent.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapCutoff.setOnClickListener { view: View -> onWordWrap(view) }
        binding.printerWordwrapIgnore.setOnClickListener { view: View -> onWordWrap(view) }

        binding.printerStylingStrikethrough.setOnClickListener { view: View -> onStyling(view) }
        binding.printerStylingUnderline.setOnClickListener { view: View -> onStyling(view) }
        binding.printerStylingItalic.setOnClickListener { view: View -> onStyling(view) }
        binding.printerStylingBold.setOnClickListener { view: View -> onStyling(view) }

        binding.printerFontsizeSmall.setOnClickListener { view: View -> onSize(view) }
        binding.printerFontsizeRegular.setOnClickListener { view: View -> onSize(view) }
        binding.printerFontsizeLarge.setOnClickListener { view: View -> onSize(view) }
        binding.printerFontsizeHuge.setOnClickListener { view: View -> onSize(view) }

        binding.printerFontSansserif.setOnClickListener { view: View -> onFont(view) }
        binding.printerFontMonospace.setOnClickListener { view: View -> onFont(view) }
        binding.printerFontSerif.setOnClickListener { view: View -> onFont(view) }

        return root
    }

    fun onWordWrap(view: View) {
        this.printerWordWrap =
            binding.printerWordwrapCutoff.flag(Printer.CUT_OFF) or
                    binding.printerWordwrapCutoffellipsis.flag(Printer.CUT_OFF_ELLIPSIS) or
                    binding.printerWordwrapIgnore.flag(Printer.IGNORE) or
                    binding.printerWordwrapPrioritiseleft.flag(Printer.PRIORITISE_LEFT) or
                    binding.printerWordwrapReversegravity.flag(Printer.REVERSE_GRAVITY) or
                    binding.printerWordwrapSplitleftright.flag(Printer.SPLIT_LEFT_RIGHT) or
                    binding.printerWordwrapWrapindent.flag(Printer.WRAP_INDENT)
    }

    fun onStyling(view: View) {
        this.printerStyling =
            binding.printerStylingBold.flag(Printer.BOLD) or
                    binding.printerStylingItalic.flag(Printer.ITALIC) or
                    binding.printerStylingUnderline.flag(Printer.UNDERLINE) or
                    binding.printerStylingStrikethrough.flag(Printer.STRIKE_THROUGH)
    }

    fun onSize(view: View) {
        this.printerFontSize = when (view.id) {
            R.id.printer_fontsize_small -> Printer.FONT_SIZE_SMALL
            R.id.printer_fontsize_regular -> Printer.FONT_SIZE_REGULAR
            R.id.printer_fontsize_large -> Printer.FONT_SIZE_LARGE
            R.id.printer_fontsize_huge -> Printer.FONT_SIZE_HUGE
            else -> Printer.FONT_SIZE_REGULAR
        }
    }

    fun onFont(view: View) {
        this.printerFont = when (view.id) {
            R.id.printer_font_sansserif -> Printer.Font.SANS_SERIF
            R.id.printer_font_serif -> Printer.Font.SERIF
            R.id.printer_font_monospace -> Printer.Font.MONOSPACE
            else -> Printer.Font.SANS_SERIF
        }
    }

    /**
     * We handle the [ScannerUtility] in [onResume] and [onPause] to make sure it's only running
     * when in the foreground
     */
    @OptIn(ExperimentalContracts::class)
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        // This is an example of how to use the ScannerUtility
        if (scanner.isSupported) {
            // We observe the error observable
            scanner.error.observe(viewLifecycleOwner) { (error, exception) ->
                if (error.isNotBlank()) {
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                    Log.w(TAG) { exception?.message ?: "No Exception" }
                }
            }

            // Variable for scanner counter
            var i = 0

            // Initialise the Scanner
            scanner.initialise(
                // We use default settings (via the default parameters) but we want continuous mode)
                DecoderSettings(ScanMode.CONTINUOUS),
                // The callback for this is then also used for the multiScanCallback
                { result ->
                    binding.scannerResult.text = getString(R.string.result) + if (result.isActualResult()) {
                        result.data
                    } else {
                        "No result"
                    }
                    binding.scannerCount.text = getString(R.string.count) + (++i)
                },
                // Here we explicitly set QR to true and otherwise use default settings
                // QR is set to true in default settings, this is merely a show of how to do it
                symbologySettings = SymbologySettings.DEFAULT.copy(qr = QrSettings(true))
            )

            // Set the scanner button
            binding.scannerButton.setOnClickListener {
                if (scanner.isGoing) {
                    scanner.stopScanning()
                } else {
                    scanner.performScanning()
                }
            }
        } else {
            binding.scannerResult.text = getString(R.string.result) + getString(R.string.device_not_applicable)
            binding.scannerButton.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun CheckBox.flag(flag: Int): Int {
        return if (this.isChecked) flag else 0
    }

    private companion object {
        private const val TAG = "UtilityFragment"
    }
}