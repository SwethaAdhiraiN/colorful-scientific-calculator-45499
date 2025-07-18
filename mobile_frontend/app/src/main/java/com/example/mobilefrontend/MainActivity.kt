package com.example.mobilefrontend

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

/**
 * PUBLIC_INTERFACE
 * MainActivity: The main entry point for the calculator app.
 * Sets up the UI, handles button input, updates the display, and evaluates mathematical expressions.
 * (Restored to non-animated, minimal original code.)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var inputExpr: String = ""
    private var lastAnswer: Double? = null

    // PUBLIC_INTERFACE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.tvDisplay)

        val buttonIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnPercent, R.id.btnLeftParen, R.id.btnRightParen,
            R.id.btnClear, R.id.btnDel, R.id.btnEquals, R.id.btnSin, R.id.btnCos, R.id.btnTan, R.id.btnLn, R.id.btnExp, R.id.btnPower, R.id.btnRoot, R.id.btnFact
        )

        for (id in buttonIds) {
            val btn = findViewById<Button>(id)
            btn?.setOnClickListener { onButtonClick(btn) }
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Handles button click logic and routes input to appropriate handler.
     */
    private fun onButtonClick(button: Button) {
        when (button.id) {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnLeftParen, R.id.btnRightParen,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnPercent,
            R.id.btnPower, R.id.btnRoot ->
                processInput(button.text.toString())
            R.id.btnSin, R.id.btnCos, R.id.btnTan, R.id.btnLn, R.id.btnExp ->
                processInput("${button.text}(")
            R.id.btnFact ->
                processInput("!")
            R.id.btnClear -> clearDisplay()
            R.id.btnDel -> deleteLast()
            R.id.btnEquals -> evaluateAndDisplay()
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Appends input to the current expression and updates the display.
     */
    private fun processInput(input: String) {
        if (inputExpr == "NaN" || inputExpr == "Infinity" || inputExpr == "Error") {
            inputExpr = ""
        }
        inputExpr += input
        updateDisplay()
    }

    /**
     * PUBLIC_INTERFACE
     * Clears all input and resets the display.
     */
    private fun clearDisplay() {
        inputExpr = ""
        updateDisplay()
    }

    /**
     * PUBLIC_INTERFACE
     * Deletes the last character of input.
     */
    private fun deleteLast() {
        if (inputExpr.isNotEmpty()) {
            inputExpr = inputExpr.dropLast(1)
            updateDisplay()
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Evaluates the entered expression, shows the result or error if evaluation fails.
     */
    private fun evaluateAndDisplay() {
        try {
            val result = evaluateExpression(inputExpr)
            display.text = result.toString()
            lastAnswer = result
            inputExpr = ""
        } catch (e: Exception) {
            display.text = "Error"
            inputExpr = ""
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Updates the calculator's display with the current expression.
     */
    private fun updateDisplay() {
        display.text = if (inputExpr.isEmpty()) "0" else inputExpr
    }

    /**
     * PUBLIC_INTERFACE
     * Safely parses and evaluates the input expression string using shunting-yard algorithm.
     *
     * Supports:
     * - Basic arithmetic: +, -, ×, ÷, ^, %
     * - Parentheses for grouping
     * - Scientific functions: sin, cos, tan, ln, exp, √, n!
     * - Decimal numbers
     */
    private fun evaluateExpression(expr: String): Double {
        val parser = CalculatorParser()
        return parser.parse(expr)
    }

    /**
     * Nested calculator parser and evaluator class.
     */
    private class CalculatorParser {
        private var pos = -1
        private var ch = 0
        private lateinit var input: String

        fun parse(str: String): Double {
            input = transformInput(str)
            pos = -1
            ch = 0
            nextChar()
            val x = parseExpression()
            if (pos < input.length) throw RuntimeException("Unexpected: '${ch.toChar()}'")
            return x
        }

        // Transform user-friendly symbols and function calls to plain-text for parsing
        private fun transformInput(str: String): String {
            return str.replace('÷', '/')
                .replace('×', '*')
                .replace('−', '-')
                .replace("%", "/100") // simple percent handling
                .replace("√", "sqrt")
                .replace("sin", "s")
                .replace("cos", "c")
                .replace("tan", "t")
                .replace("ln", "l")
                .replace("exp", "e")
        }

        private fun nextChar() {
            ch = if (++pos < input.length) input[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | number | functionName factor | '(' expression ')' | factor `^` factor | factor '!'
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            when {
                eat('('.code) -> {
                    x = parseExpression()
                    eat(')'.code)
                }
                ch in '0'.code..'9'.code || ch == '.'.code -> {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = input.substring(startPos, pos).toDouble()
                }
                ch in 'a'.code..'z'.code -> {
                    while (ch in 'a'.code..'z'.code) nextChar()
                    val func = input.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "s" -> sin(Math.toRadians(x))
                        "c" -> cos(Math.toRadians(x))
                        "t" -> tan(Math.toRadians(x))
                        "l" -> ln(x)
                        "e" -> exp(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                }
                else -> throw RuntimeException("Unexpected character: '${ch.toChar()}'")
            }

            if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation
            if (eat('!'.code)) x = factorial(x)
            return x
        }

        private fun factorial(x: Double): Double {
            if (x < 0) throw RuntimeException("Negative factorial")
            if (x % 1 != 0.0) throw RuntimeException("Non-integer factorial")
            return if (x == 0.0) 1.0 else x * factorial(x - 1)
        }
    }
}
