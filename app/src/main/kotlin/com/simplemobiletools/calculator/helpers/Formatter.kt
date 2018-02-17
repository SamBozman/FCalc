package com.simplemobiletools.calculator.helpers

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object Formatter {
    fun doubleToString(d: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.decimalSeparator = '.'
        symbols.groupingSeparator = ','

        val formatter = DecimalFormat()
        formatter.maximumFractionDigits = 12
        formatter.decimalFormatSymbols = symbols
        formatter.isGroupingUsed = true
        return formatter.format(d)
    }
        //This function removes all the commas from the submitted String
        // and then returns a Double number representation of the String
    fun stringToDouble(str: String) = str.replace(",", "").toDouble()
}
