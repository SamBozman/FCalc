package com.simplemobiletools.calculator.helpers

import android.content.Context
//import android.widget.Toast
import com.simplemobiletools.calculator.R
import com.simplemobiletools.calculator.operation.OperationFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.round

class CalculatorImpl(calculator: Calculator, val context: Context) {
    var displayedNumber: String? = null
    var displayedFormula: String? = null
    var displayedFraction: String? = null

    var lastKey: String? = null
    private var mLastOperation: String? = null
    private var mCallback: Calculator? = calculator

    private var mIsFirstOperation = false
    private var mResetValue = false
    private var mBaseValue = 0.0
    private var mSecondValue = 0.0
    var mDenominator = 16

    init {
        resetValues()
        setValue("0")
        setFormula("")
        setFraction("")
    }

    private fun resetValueIfNeeded() {
        if (mResetValue) // If True then displayedNumber = 0
            displayedNumber = "0"

        mResetValue = false // Then reset to false
    }

    private fun resetValues() {
        mBaseValue = 0.0
        mSecondValue = 0.0
        mResetValue = false
        mLastOperation = ""
        displayedNumber = ""
        displayedFraction = ""
        displayedFormula = ""
        mIsFirstOperation = true
        lastKey = ""
    }

    fun setValue(value: String) {
        mCallback!!.setValue(value, context)
        displayedNumber = value
    }

    private fun setFormula(value: String) {
        mCallback!!.setFormula(value, context)
        displayedFormula = value
    }

    private fun setFraction(value: String) {
        mCallback!!.setFraction(value, context)
        displayedFraction = value
    }

    private fun showToastMessage(value: String){
        mCallback!!.showToastMessage(value, context)
    }

    private fun updateFormula() {
        val first = Formatter.doubleToString(mBaseValue)
        val second = Formatter.doubleToString(mSecondValue)
        val sign = getSign(mLastOperation)

        if (sign == "√") {
            setFormula(sign + first)
        } else if (!sign.isEmpty()) {
            setFormula(first + sign + second)
        }
    }

    fun addDigit(number: Int) {
        val currentValue = displayedNumber //displayedNumber is a String

        //you can concatenate string literal with integer by + operator
        val newValue = formatString(currentValue!! + number)
        setValue(newValue)
    }

    private fun formatString(str: String): String {
        // if the number contains a decimal, do not try removing the leading zero anymore, nor add group separator
        // it would prevent writing values like 1.02
        if (str.contains(".")) {
            return str
        }

        //reformats the String (to reposition the commas) and then returns a Double number representation of the submitted String
        val doubleValue = Formatter.stringToDouble(str)

        //changes the Double into a String with commas in the correct place
        return Formatter.doubleToString(doubleValue) //Returns the reformatted String
    }

    private fun updateResult(value: Double) {
        setValue(Formatter.doubleToString(value))
        mBaseValue = value
    }
//This function
    private fun getDisplayedNumberAsDouble() = Formatter.stringToDouble(displayedNumber!!)

    fun handleResult() {
        mSecondValue = getDisplayedNumberAsDouble()
        calculateResult()
        mBaseValue = getDisplayedNumberAsDouble()
    }

    private fun handleRoot() {
        mBaseValue = getDisplayedNumberAsDouble()
        calculateResult()
    }

    private fun calculateResult() {
        if (!mIsFirstOperation) {
            updateFormula()
        }

        val operation = OperationFactory.forId(mLastOperation!!, mBaseValue, mSecondValue)

        if (operation != null) {
            updateResult(operation.getResult())
        }

        mIsFirstOperation = false
    }

    fun handleOperation(operation: String) {
        if (lastKey == DIGIT && operation != ROOT)
            handleResult() //

        mResetValue = true
        lastKey = operation
        mLastOperation = operation

        if (operation == ROOT) {
            handleRoot()
            mResetValue = false
        }
    }

    fun handleDiFract(){
        var currentValue = displayedNumber
        var decimalPointIndex = currentValue!!.lastIndexOf(".")

        //retrieve decimal point position in string. (-1 indicates it was not found)
        if(decimalPointIndex != -1){

            //retrieve the mantissa of the displayedNumber
            val mMantissa = currentValue.substring(decimalPointIndex)
            //Round (up or Down) Mantissa
            var mNumerator = (Math.round( mMantissa.toDouble()*mDenominator.toDouble())).toInt()


            //Set Fraction TextView
            if(mNumerator!=0){
                var g1 = mNumerator
                var g2 = mDenominator
                var result = GCD(g1,g2)
                g1 /= result
                g2 /= result
                setFraction(g1.toString() + "/" + (g2.toString()))
                showToastMessage("Fractional value of Mantissa to nearest "+ mDenominator.toString())

            }
        }
        else{
            showToastMessage("Mantissa not found")
        }


    }

    //Find GCD of two numbers TO BE TESTED
    fun GCD(t1: Int, t2: Int): Int {
        var n1 = t1
        var n2 = t2
        while (n1 != n2) {
            if (n1 > n2)
                n1 -= n2
            else
                n2 -= n1
        }
        return n1
    }



    fun handleClear() {
        if (displayedNumber.equals(NAN)) {
            handleReset()
        } else {
            val oldValue = displayedNumber
            var newValue = "0"
            val len = oldValue!!.length
            var minLen = 1
            if (oldValue.contains("-"))
                minLen++

            if (len > minLen) {
                newValue = oldValue.substring(0, len - 1)
            }

            newValue = newValue.replace("\\.$".toRegex(), "")
            newValue = formatString(newValue)
            setValue(newValue)
            mBaseValue = Formatter.stringToDouble(newValue)
        }
    }

    fun handleReset() {
        resetValues()
        setValue("0")
        setFormula("")
        setFraction("")
    }

    fun handleEquals() {

        if (lastKey == EQUALS)// If this is the second time for 'Equals" then
            calculateResult()

        if (lastKey != DIGIT)
            return

        mSecondValue = getDisplayedNumberAsDouble()
        calculateResult()
        lastKey = EQUALS
    }

    private fun decimalClicked() {
        var value = displayedNumber
        if (!value!!.contains(".")) {
            value += "."
        }
        setValue(value)
    }

    private fun zeroClicked() {
        val value = displayedNumber
        if (value != "0")
            addDigit(0)
    }

    private fun getSign(lastOperation: String?) = when (lastOperation) {
        PLUS -> "+"
        MINUS -> "-"
        MULTIPLY -> "*"
        DIVIDE -> "/"
        MODULO -> "%"
        POWER -> "^"
        ROOT -> "√"
        else -> ""
    }

    fun numpadClicked(id: Int) {
        if (lastKey == EQUALS) {
            mLastOperation = EQUALS
        }

        lastKey = DIGIT
        resetValueIfNeeded()

        when (id) {
            R.id.btn_decimal -> decimalClicked()
            R.id.btn_0 -> zeroClicked()
            R.id.btn_1 -> addDigit(1)
            R.id.btn_2 -> addDigit(2)
            R.id.btn_3 -> addDigit(3)
            R.id.btn_4 -> addDigit(4)
            R.id.btn_5 -> addDigit(5)
            R.id.btn_6 -> addDigit(6)
            R.id.btn_7 -> addDigit(7)
            R.id.btn_8 -> addDigit(8)
            R.id.btn_9 -> addDigit(9)
        }
    }
}
