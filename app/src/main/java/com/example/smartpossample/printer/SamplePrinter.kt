package com.example.smartpossample.printer

import android.content.Context
import com.example.smartpossample.R
import com.google.zxing.BarcodeFormat
import eu.nets.lab.smartpos.sdk.Log
import eu.nets.lab.smartpos.sdk.utility.printer.*
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.BOLD
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.CUT_OFF
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.DEFAULT
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.FONT_SIZE_LARGE
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.FONT_SIZE_REGULAR
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.FONT_SIZE_SMALL
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.NORMAL
import eu.nets.lab.smartpos.sdk.utility.printer.Printer.SPLIT_LEFT_RIGHT
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is not in the tutorial, but can be used as an example of how to use the
 * [PrinterUtility] utility included in the SDK
 */
class SamplePrinter (private val context: Context) {
    fun printShort(
            @Printer.FontSize fontSize: Int,
            wordWrap: Int,
            styling: Int,
            font: Printer.Font,
    ) {
        printTest("This is short", fontSize, wordWrap, styling, font)
    }

    fun printLong(
            @Printer.FontSize fontSize: Int,
            wordWrap: Int,
            styling: Int,
            font: Printer.Font,
    ) {
        printTest("This is a longer test, it will probably cause some wrapping", fontSize, wordWrap, styling, font)
    }

    fun printTheTragedy(
            @Printer.FontSize fontSize: Int,
            wordWrap: Int,
            styling: Int,
            font: Printer.Font,
    ) {
        val ridiculous = "Did you ever hear the tragedy of Darth Plagueis The Wise? I thought " +
                "not. It's not a story the Jedi would tell you. It's a Sith legend. Darth " +
                "Plagueis was a Dark Lord of the Sith, so powerful and so wise he could use the " +
                "Force to influence the midichlorians to create life… He had such a knowledge of " +
                "the dark side that he could even keep the ones he cared about from dying. The " +
                "dark side of the Force is a pathway to many abilities some consider to be " +
                "unnatural. He became so powerful… the only thing he was afraid of was losing " +
                "his power, which eventually, of course, he did. Unfortunately, he taught his " +
                "apprentice everything he knew, then his apprentice killed him in his sleep. " +
                "Ironic. He could save others from death, but not himself."
        printTest(ridiculous, fontSize, wordWrap, styling, font)
    }

    private fun printTest(
            text: String,
            @Printer.FontSize fontSize: Int,
            wordWrap: Int,
            styling: Int,
            font: Printer.Font,
    ) {
        val utility = PrinterUtility()

        val page = Page()
        val expl = LeftFactory(FONT_SIZE_SMALL, DEFAULT, NORMAL, Printer.Font.SANS_SERIF)
        page += expl("Left: ")
        page += Left(text, fontSize, wordWrap, styling, font)
        page += HorizontalLine(HorizontalLine.Thickness.THICK)
        page += expl("Right: ")
        page += Right(text, fontSize, wordWrap, styling, font)
        page += HorizontalLine(HorizontalLine.Thickness.THICK)
        page += expl("Centered: ")
        page += Centered(text, fontSize, wordWrap, styling, font)
        utility.addPage(page)
        utility.print(true)
    }

    fun jokeyTestReceipt(explained: Boolean) {

        val utility = PrinterUtility()

        val expl = LeftFactory(FONT_SIZE_SMALL, DEFAULT, NORMAL, Printer.Font.SANS_SERIF)

        val header = Page()
        // TODO Add a plus assign operator
        if (explained) header += expl("Centered{FONT_SIZE_LARGE, DEFAULT, BOLD, Font.SERIF}")
        header += Centered("Emerging Products Kiosk", FONT_SIZE_LARGE, DEFAULT, BOLD, Printer.Font.SERIF)
        if (explained) header += expl("Centered{FONT_SIZE_REGULAR, DEFAULT, NORMAL, Font.SANS_SERIF}")
        header += Centered("Your all-in-one regular kiosk shop", FONT_SIZE_REGULAR, DEFAULT, NORMAL, Printer.Font.SANS_SERIF)
        if (explained) header += expl("Picture")
        header += Picture(context.resources, R.drawable.nets_logo_black, null, 50)
        if (explained) header += expl("Centered{FONT_SIZE_SMALL, DEFAULT, NORMAL, Font.SANS_SERIF}")
        header += Centered("Open 24.61/7/687 ", FONT_SIZE_SMALL, 0, 0, Printer.Font.SANS_SERIF)
        if (explained) header += expl("HorizontalLine{Thickness.THICK}")
        header += HorizontalLine(HorizontalLine.Thickness.THICK)
        val date = Date()
        val dateF = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)
        val timeF = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
        if (explained) header += expl("LeftRight{FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT, NORMAL, Font.MONOSPACE}")
        header += LeftRight(dateF.format(date), timeF.format(date), FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT, 0, Printer.Font.MONOSPACE)
        utility.addPage(header)

        val itemFactory = LeftRightFactory(FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT or CUT_OFF, 0, Printer.Font.SANS_SERIF)
        val items = Page()
        if (explained) items += expl("LeftRight{FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT | CUT_OFF, NORMAL, Font.SANS_SERIF}")
        items += itemFactory("Medium Wraps", "$ 1.99")
        items += itemFactory("Medium Wraps", "$ 1.99")
        items += itemFactory("Toms Gold Bar", "$ 0.99")
        items += itemFactory("Thermonuclear warhead", "$ 399.00")
        items += itemFactory("SW Roastbeef", "$ 0.49")
        items += itemFactory("Milk 0.5%", "$ 0.99")
        items += itemFactory("Shuttlecock 3 pack", "$ 1.29")
        if (explained) items += expl("HorizontalLine{Thickness.THIN}")
        items += HorizontalLine(HorizontalLine.Thickness.THIN)
        if (explained) items += expl("LeftRight{FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT, BOLD, Font.SANS_SERIF}")
        items += LeftRight("Total", "$ 406.74", FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT, BOLD, Printer.Font.SANS_SERIF)

        if (explained) items += expl("Barcode{\"https://nets.eu\", QR_CODE, 300, 300}")
        items += Barcode("https://nets.eu", BarcodeFormat.QR_CODE, 300, 300, null)

        utility.addPage(items)

        val paid = Page()
        if (explained) paid += expl("LeftRight{FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT | CUT_OFF, NORMAL, Font.SANS_SERIF}")
        paid += itemFactory("To pay", "$ 406.74")
        if (explained) paid += expl("Empty{FONT_SIZE_SMALL (18)}")
        paid += Empty(FONT_SIZE_SMALL)
        if (explained) paid += expl("LeftRight{FONT_SIZE_REGULAR, SPLIT_LEFT_RIGHT | CUT_OFF, NORMAL, Font.SANS_SERIF}")
        paid += itemFactory("Paid card", "$ 506.74")
        paid += itemFactory("Cashback", "$ 100.00")

        utility.addPage(paid)

        val cenFact = CenteredFactory(FONT_SIZE_REGULAR, 0, BOLD, Printer.Font.SANS_SERIF)
        val thank = Page()
        if (explained) thank += expl("Centered{FONT_SIZE_REGULAR, DEFAULT, BOLD, Font.SANS_SERIF}")
        thank += cenFact("Thank you for shopping at Emerging Products Kiosk.")
        cenFact.fontSize = FONT_SIZE_SMALL
        if (explained) thank += expl("Centered{FONT_SIZE_SMALL, DEFAULT, BOLD, Font.SANS_SERIF}")
        thank += cenFact("You are not allowed to use any products sold at Emerging Products " +
                "Kiosk as weapons. Emerging Products Kiosk takes no responsibility for any damages" +
                " caused by Emerging Products products. Any damages done by Emerging Products Kiosk " +
                "products, including, but not limited to, nuclear warheads, are the responsibility " +
                "of the customer.")
        if (explained) thank += expl("Barcode{\"https://nets.eu\", EAN_13, 300, 100}")
        thank += Barcode("BAR CODE", BarcodeFormat.CODE_39, 300, 100, null)

        utility.addPage(thank)

        Log.i(TAG) { "The receipt: \n" + utility.getReceipt() }

        utility.print()
    }

    companion object {
        private const val TAG = "SamplePrinter"
    }
}