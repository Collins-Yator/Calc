package com.example.calculator.engine

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
    DEG,
    RAD
}

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

object ExpressionEvaluator {

    const val PI_SYMBOL = "π"
    const val E_SYMBOL = "e"
    const val SQRT_SYMBOL = "√"
    const val CBRT_SYMBOL = "∛"

    /**
     * Evaluates a mathematical expression string given the angle mode (DEG or RAD).
     */
    fun evaluate(expression: String, angleMode: AngleMode): EvalResult {
        val cleanExpr = sanitize(expression)
        if (cleanExpr.isBlank()) {
            return EvalResult.Error("Empty expression")
        }

        return try {
            val tokens = tokenize(cleanExpr)
            if (tokens.isEmpty()) {
                return EvalResult.Error("Empty expression")
            }
            val parser = Parser(tokens, angleMode)
            val result = parser.parseExpression()

            if (parser.hasMoreTokens()) {
                return EvalResult.Error("Unexpected symbol at end")
            }

            if (result.isNaN()) {
                return EvalResult.Error("Invalid operation (NaN)")
            }
            if (result.isInfinite()) {
                return EvalResult.Error(if (result > 0) "Infinity" else "-Infinity")
            }

            val formatted = formatResult(result)
            EvalResult.Success(result, formatted)
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(e.message ?: "Invalid syntax")
        } catch (e: Exception) {
            EvalResult.Error(e.message ?: "Error")
        }
    }

    /**
     * Attempts a quiet live-preview evaluation while the user is typing.
     * Returns null if incomplete, unparseable, or erroneous.
     */
    fun preview(expression: String, angleMode: AngleMode): String? {
        val cleanExpr = sanitize(expression).trim()
        if (cleanExpr.isBlank()) return null

        var balancedExpr = cleanExpr
        val openCount = balancedExpr.count { it == '(' }
        val closeCount = balancedExpr.count { it == ')' }
        if (openCount > closeCount) {
            balancedExpr += ")".repeat(openCount - closeCount)
        }

        val trimmed = balancedExpr.trimEnd { it in "+-×*÷/^(" }
        if (trimmed.isBlank()) return null

        return when (val eval = evaluate(trimmed, angleMode)) {
            is EvalResult.Success -> {
                if (trimmed.toDoubleOrNull() != null) null else eval.formatted
            }
            is EvalResult.Error -> null
        }
    }

    fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("·", "*")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("sinh⁻¹", "asinh")
            .replace("cosh⁻¹", "acosh")
            .replace("tanh⁻¹", "atanh")
            .replace("10ˣ", "10^")
            .replace("eˣ", "exp")
            .replace("log₁₀", "log")
            .replace(" ", "")
    }

    fun formatResult(value: Double): String {
        if (value.isFinite() && abs(value) < 1e14 && value == floor(value)) {
            val longVal = value.toLong()
            return DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(longVal)
        }

        val rounded = if (abs(value) < 1e-12) 0.0 else value

        if (abs(rounded) >= 1e12 || (abs(rounded) > 0 && abs(rounded) < 1e-6)) {
            val sciFormat = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return sciFormat.format(rounded).replace("E", " × 10^")
        }

        val df = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US))
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(rounded)
    }

    // -------------------------------------------------------------
    // Tokenizer & Parser
    // -------------------------------------------------------------

    private enum class TokenType {
        NUMBER,
        IDENTIFIER,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        MODULO,
        POWER,
        FACTORIAL,
        PERCENT,
        LPAREN,
        RPAREN,
        COMMA
    }

    private data class Token(val type: TokenType, val text: String, val value: Double = 0.0)

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]

            when {
                c.isWhitespace() -> {
                    i++
                }
                c == 'π' -> {
                    tokens.add(Token(TokenType.NUMBER, PI_SYMBOL, Math.PI))
                    i++
                }
                c == '√' -> {
                    tokens.add(Token(TokenType.IDENTIFIER, "sqrt"))
                    i++
                }
                c == '∛' -> {
                    tokens.add(Token(TokenType.IDENTIFIER, "cbrt"))
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = c == '.'
                    i++
                    while (i < len) {
                        val next = input[i]
                        if (next.isDigit()) {
                            i++
                        } else if (next == '.' && !hasDot) {
                            hasDot = true
                            i++
                        } else {
                            break
                        }
                    }
                    val numStr = input.substring(start, i)
                    val numVal = numStr.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Invalid number: $numStr")
                    tokens.add(Token(TokenType.NUMBER, numStr, numVal))
                }
                c.isLetter() -> {
                    val start = i
                    i++
                    while (i < len && input[i].isLetter()) {
                        i++
                    }
                    // Special case for log10 or log2
                    if (i < len && (input[i] == '1' || input[i] == '2')) {
                        if (input.substring(start, i) == "log") {
                            if (input.startsWith("log10", start)) {
                                i = start + 5
                            } else if (input.startsWith("log2", start)) {
                                i = start + 4
                            }
                        }
                    }
                    val word = input.substring(start, i)
                    when (word) {
                        "e" -> tokens.add(Token(TokenType.NUMBER, E_SYMBOL, Math.E))
                        "pi" -> tokens.add(Token(TokenType.NUMBER, PI_SYMBOL, Math.PI))
                        else -> tokens.add(Token(TokenType.IDENTIFIER, word))
                    }
                }
                c == '+' -> {
                    tokens.add(Token(TokenType.PLUS, "+"))
                    i++
                }
                c == '-' -> {
                    tokens.add(Token(TokenType.MINUS, "-"))
                    i++
                }
                c == '*' -> {
                    tokens.add(Token(TokenType.MULTIPLY, "*"))
                    i++
                }
                c == '/' -> {
                    tokens.add(Token(TokenType.DIVIDE, "/"))
                    i++
                }
                c == '%' -> {
                    tokens.add(Token(TokenType.PERCENT, "%"))
                    i++
                }
                c == '^' -> {
                    tokens.add(Token(TokenType.POWER, "^"))
                    i++
                }
                c == '!' -> {
                    tokens.add(Token(TokenType.FACTORIAL, "!"))
                    i++
                }
                c == '(' -> {
                    tokens.add(Token(TokenType.LPAREN, "("))
                    i++
                }
                c == ')' -> {
                    tokens.add(Token(TokenType.RPAREN, ")"))
                    i++
                }
                c == ',' -> {
                    tokens.add(Token(TokenType.COMMA, ","))
                    i++
                }
                else -> {
                    throw IllegalArgumentException("Unexpected character: '$c'")
                }
            }
        }

        // Implicit multiplication insertion:
        val processed = mutableListOf<Token>()
        for (idx in tokens.indices) {
            val curr = tokens[idx]
            processed.add(curr)
            if (idx + 1 < tokens.size) {
                val next = tokens[idx + 1]
                val isCurrConstant = curr.text == PI_SYMBOL || curr.text == E_SYMBOL
                val isNextConstant = next.text == PI_SYMBOL || next.text == E_SYMBOL

                val needsImplicitMult = when {
                    // NUMBER followed by LPAREN (2(3) -> 2*(3))
                    (curr.type == TokenType.NUMBER && next.type == TokenType.LPAREN) -> true
                    // NUMBER followed by IDENTIFIER (2sin(x) -> 2*sin(x), 2sqrt(x) -> 2*sqrt(x))
                    (curr.type == TokenType.NUMBER && next.type == TokenType.IDENTIFIER) -> true
                    // NUMBER followed by CONSTANT (2π -> 2*π)
                    (curr.type == TokenType.NUMBER && isNextConstant) -> true
                    // CONSTANT followed by NUMBER (π2 -> π*2)
                    (isCurrConstant && next.type == TokenType.NUMBER) -> true
                    // CONSTANT followed by CONSTANT (πe -> π*e)
                    (isCurrConstant && isNextConstant) -> true
                    // RPAREN followed by LPAREN, NUMBER, IDENTIFIER, or CONSTANT
                    (curr.type == TokenType.RPAREN && (next.type == TokenType.LPAREN || next.type == TokenType.NUMBER || next.type == TokenType.IDENTIFIER || isNextConstant)) -> true
                    // FACTORIAL / PERCENT followed by NUMBER, LPAREN, IDENTIFIER, or CONSTANT
                    ((curr.type == TokenType.FACTORIAL || curr.type == TokenType.PERCENT) && (next.type == TokenType.NUMBER || next.type == TokenType.LPAREN || next.type == TokenType.IDENTIFIER || isNextConstant)) -> true
                    else -> false
                }
                if (needsImplicitMult) {
                    processed.add(Token(TokenType.MULTIPLY, "*"))
                }
            }
        }

        return processed
    }

    private class Parser(private val tokens: List<Token>, private val angleMode: AngleMode) {
        private var pos = 0

        fun hasMoreTokens(): Boolean = pos < tokens.size

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null

        private fun consume(): Token {
            if (pos >= tokens.size) throw IllegalArgumentException("Unexpected end of expression")
            return tokens[pos++]
        }

        private fun match(type: TokenType): Boolean {
            if (peek()?.type == type) {
                pos++
                return true
            }
            return false
        }

        fun parseExpression(): Double {
            return parseAddSubtract()
        }

        private fun parseAddSubtract(): Double {
            var left = parseMultiplyDivide()
            while (true) {
                val token = peek() ?: break
                when (token.type) {
                    TokenType.PLUS -> {
                        consume()
                        left += parseMultiplyDivide()
                    }
                    TokenType.MINUS -> {
                        consume()
                        left -= parseMultiplyDivide()
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parseMultiplyDivide(): Double {
            var left = parseExponent()
            while (true) {
                val token = peek() ?: break
                when (token.type) {
                    TokenType.MULTIPLY -> {
                        consume()
                        left *= parseExponent()
                    }
                    TokenType.DIVIDE -> {
                        consume()
                        val right = parseExponent()
                        if (abs(right) < 1e-15) {
                            throw ArithmeticException("Cannot divide by zero")
                        }
                        left /= right
                    }
                    TokenType.MODULO -> {
                        consume()
                        val right = parseExponent()
                        if (abs(right) < 1e-15) {
                            throw ArithmeticException("Cannot divide by zero")
                        }
                        left %= right
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parseExponent(): Double {
            val left = parseUnary()
            if (match(TokenType.POWER)) {
                val right = parseExponent()
                if (left < 0 && right != floor(right)) {
                    throw ArithmeticException("Domain error: Negative base with fractional exponent")
                }
                return left.pow(right)
            }
            return left
        }

        private fun parseUnary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")
            if (token.type == TokenType.PLUS) {
                consume()
                return parseUnary()
            }
            if (token.type == TokenType.MINUS) {
                consume()
                return -parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                val token = peek() ?: break
                when (token.type) {
                    TokenType.FACTORIAL -> {
                        consume()
                        value = computeFactorial(value)
                    }
                    TokenType.PERCENT -> {
                        consume()
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            when (token.type) {
                TokenType.NUMBER -> {
                    consume()
                    return token.value
                }
                TokenType.LPAREN -> {
                    consume()
                    val result = parseExpression()
                    if (!match(TokenType.RPAREN)) {
                        throw IllegalArgumentException("Missing closing parenthesis ')'")
                    }
                    return result
                }
                TokenType.IDENTIFIER -> {
                    consume()
                    return parseFunction(token.text)
                }
                else -> {
                    throw IllegalArgumentException("Unexpected token '${token.text}'")
                }
            }
        }

        private fun parseFunction(name: String): Double {
            val lower = name.lowercase(Locale.ROOT)

            val arg = if (match(TokenType.LPAREN)) {
                val res = parseExpression()
                if (!match(TokenType.RPAREN)) {
                    throw IllegalArgumentException("Missing ')' after $name argument")
                }
                res
            } else {
                parseUnary()
            }

            return when (lower) {
                "sin" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    sin(rad)
                }
                "cos" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    cos(rad)
                }
                "tan" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    if (abs(cos(rad)) < 1e-12) {
                        throw ArithmeticException("Tangent undefined at 90° / 270°")
                    }
                    tan(rad)
                }
                "asin", "arcsin" -> {
                    if (arg < -1.0 || arg > 1.0) {
                        throw ArithmeticException("Domain error: asin argument must be between -1 and 1")
                    }
                    val rad = asin(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "acos", "arccos" -> {
                    if (arg < -1.0 || arg > 1.0) {
                        throw ArithmeticException("Domain error: acos argument must be between -1 and 1")
                    }
                    val rad = acos(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "atan", "arctan" -> {
                    val rad = atan(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "asinh" -> ln(arg + sqrt(arg * arg + 1.0))
                "acosh" -> {
                    if (arg < 1.0) throw ArithmeticException("Domain error: acosh requires x >= 1")
                    ln(arg + sqrt(arg * arg - 1.0))
                }
                "atanh" -> {
                    if (arg <= -1.0 || arg >= 1.0) throw ArithmeticException("Domain error: atanh requires -1 < x < 1")
                    0.5 * ln((1.0 + arg) / (1.0 - arg))
                }
                "sqrt" -> {
                    if (arg < 0) throw ArithmeticException("Domain error: square root of negative number")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "ln" -> {
                    if (arg <= 0) throw ArithmeticException("Domain error: ln requires positive number")
                    ln(arg)
                }
                "log", "log10" -> {
                    if (arg <= 0) throw ArithmeticException("Domain error: log requires positive number")
                    log10(arg)
                }
                "log2" -> {
                    if (arg <= 0) throw ArithmeticException("Domain error: log2 requires positive number")
                    ln(arg) / ln(2.0)
                }
                "exp" -> exp(arg)
                "abs" -> abs(arg)
                "recip" -> {
                    if (abs(arg) < 1e-15) throw ArithmeticException("Cannot divide by zero")
                    1.0 / arg
                }
                else -> throw IllegalArgumentException("Unknown function '$name'")
            }
        }

        private fun computeFactorial(n: Double): Double {
            if (n < 0 || n != floor(n)) {
                throw ArithmeticException("Factorial is only defined for non-negative integers")
            }
            if (n > 170) {
                throw ArithmeticException("Overflow: Factorial too large")
            }
            val intN = n.toInt()
            var result = 1.0
            for (k in 2..intN) {
                result *= k
            }
            return result
        }
    }
}
