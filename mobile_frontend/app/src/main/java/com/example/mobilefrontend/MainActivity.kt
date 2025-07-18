package com.example.mobilefrontend

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import kotlin.math.*

/**
 * PUBLIC_INTERFACE
 * MainActivity: The main entry point for the calculator app.
 * Sets up the UI, handles all button input, updates the display, and evaluates mathematical expressions.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private lateinit var header: TextView
    private var inputExpr: String = ""
    private var lastAnswer: Double? = null

    // PUBLIC_INTERFACE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        header = findViewById(R.id.tvHeader)
        display = findViewById(R.id.tvDisplay)

        // Animate the header (splash entry effect)
        header.visibility = View.INVISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            header.visibility = View.VISIBLE
            header.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_header_in))
        }, 150)

        // Animate the calculator display at start (fade-slide in)
        display.visibility = View.INVISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            display.visibility = View.VISIBLE
            display.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up))
        }, 300)

        val buttonIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnPercent, R.id.btnLeftParen, R.id.btnRightParen,
            R.id.btnClear, R.id.btnDel, R.id.btnEquals, R.id.btnSin, R.id.btnCos, R.id.btnTan, R.id.btnLn, R.id.btnExp, R.id.btnPower, R.id.btnRoot, R.id.btnFact
        )

        for (id in buttonIds) {
            val btn = findViewById<Button>(id)
            btn?.let { b ->
                // Set a modern ripple background for engaging feedback
                b.background = ContextCompat.getDrawable(this, R.drawable.btn_ripple)
                b.setOnClickListener { onButtonClick(b) }
                // Bounce/shake effect for number/operator input on press
                b.setOnTouchListener { v, event ->
                    v.animate().cancel()
                    v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(70).withEndAction {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(110).start()
                    }.start()
                    false
                }
            }
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
     * Appends input to the current expression and updates the display with fade/slide animation.
     */
    private fun processInput(input: String) {
        if (inputExpr == "NaN" || inputExpr == "Infinity" || inputExpr == "Error") {
            inputExpr = ""
        }
        inputExpr += input
        animateInputEntry()
    }

    // Animates the input entry area with fade/slide effect
    private fun animateInputEntry() {
        display.clearAnimation()
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up)
        display.startAnimation(anim)
        updateDisplay()
    }

    /**
     * PUBLIC_INTERFACE
     * Clears all input and resets the display (animated clear).
     */
    private fun clearDisplay() {
        inputExpr = ""
        display.animate().alpha(0f).setDuration(100).withEndAction {
            updateDisplay()
            display.animate().alpha(1f).setDuration(120).start()
        }.start()
    }

    /**
     * PUBLIC_INTERFACE
     * Deletes the last character of input with a quick shake animation.
     */
    private fun deleteLast() {
        if (inputExpr.isNotEmpty()) {
            inputExpr = inputExpr.dropLast(1)
            display.animate().translationX(-16f).setDuration(60)
                .withEndAction {
                    display.animate().translationX(0f).setDuration(80).start()
                    updateDisplay()
                }.start()
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Evaluates the entered expression, shows the result, or error if evaluation fails,
     * with animated transition highlighting result.
     */
    private fun evaluateAndDisplay() {
        try {
            val result = evaluateExpression(inputExpr)
            val output = result.toString()
            display.animate().alpha(0f).setDuration(110).withEndAction {
                display.setTextColor(ContextCompat.getColor(this, R.color.primary))
                display.text = output
                lastAnswer = result
                inputExpr = ""
                display.animate().alpha(1f).setDuration(170).withEndAction {
                    // revert color after a moment for minimalism
                    Handler(Looper.getMainLooper()).postDelayed({
                        display.setTextColor(ContextCompat.getColor(this, R.color.black))
                        updateDisplay()
                    }, 540)
                }.start()
            }.start()
        } catch (e: Exception) {
            display.animate().alpha(0f).setDuration(110).withEndAction {
                display.setTextColor(ContextCompat.getColor(this, R.color.extra))
                display.text = "Error"
                inputExpr = ""
                display.animate().alpha(1f).setDuration(140).withEndAction {
                    Handler(Looper.getMainLooper()).postDelayed({
                        display.setTextColor(ContextCompat.getColor(this, R.color.black))
                        updateDisplay()
                    }, 670)
                }.start()
            }.start()
        }
    }

    /**
     * PUBLIC_INTERFACE
     * Updates the calculator's display with the current expression (smooth transition on change).
     */
    private fun updateDisplay() {
        val txt = if (inputExpr.isEmpty()) "0" else inputExpr
        if (display.text != txt) {
            display.animate().alpha(0.5f).setDuration(80).withEndAction {
                display.text = txt
                display.animate().alpha(1f).setDuration(100).start()
            }.start()
        } else {
            display.text = txt // fallback
        }
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
        // factor = `+` factor | `-` factor | number | functionName factor | '(' expression ')' | factor '^' factor | factor '!'
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
