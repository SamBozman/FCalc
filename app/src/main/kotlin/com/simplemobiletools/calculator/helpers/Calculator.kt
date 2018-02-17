package com.simplemobiletools.calculator.helpers

import android.content.Context

interface Calculator {
    fun setValue(value: String, context: Context)
    fun setFraction(value: String, context: Context)
    fun setValueDouble(d: Double)
    fun showToastMessage(value: String,context: Context)
    fun setFormula(value: String, context: Context)
}
