package com.example

import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.EvalResult
import com.example.calculator.engine.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExpressionEvaluatorTest {

    private fun eval(expr: String, angleMode: AngleMode = AngleMode.DEG): Double {
        val result = ExpressionEvaluator.evaluate(expr, angleMode)
        assertTrue("Expected success for '$expr', got $result", result is EvalResult.Success)
        return (result as EvalResult.Success).value
    }

    private fun assertClose(expected: Double, actual: Double, epsilon: Double = 1e-6) {
        assertTrue("Expected $expected but was $actual", abs(expected - actual) < epsilon)
    }

    @Test
    fun testBasicArithmetic() {
        assertClose(7.0, eval("3 + 4"))
        assertClose(15.0, eval("3 * 5"))
        assertClose(15.0, eval("3 × 5"))
        assertClose(2.5, eval("5 / 2"))
        assertClose(2.5, eval("5 ÷ 2"))
        assertClose(-2.0, eval("3 - 5"))
        assertClose(-2.0, eval("3 − 5"))
    }

    @Test
    fun testOrderOfOperations() {
        // Multiplication before addition: 2 + 3 * 4 = 14
        assertClose(14.0, eval("2 + 3 * 4"))
        // Parentheses override: (2 + 3) * 4 = 20
        assertClose(20.0, eval("(2 + 3) * 4"))
        // Exponents before multiplication: 2 * 3 ^ 2 = 18
        assertClose(18.0, eval("2 * 3 ^ 2"))
        // Right-associative exponent: 2 ^ 3 ^ 2 = 2 ^ 9 = 512
        assertClose(512.0, eval("2 ^ 3 ^ 2"))
    }

    @Test
    fun testImplicitMultiplication() {
        assertClose(12.0, eval("3(4)"))
        assertClose(20.0, eval("(2 + 3)(4)"))
        assertClose(6.0, eval("2(1 + 2)"))
    }

    @Test
    fun testTrigonometryDeg() {
        assertClose(0.5, eval("sin(30)", AngleMode.DEG))
        assertClose(0.5, eval("cos(60)", AngleMode.DEG))
        assertClose(1.0, eval("tan(45)", AngleMode.DEG))
        assertClose(30.0, eval("asin(0.5)", AngleMode.DEG))
        assertClose(60.0, eval("acos(0.5)", AngleMode.DEG))
        assertClose(45.0, eval("atan(1)", AngleMode.DEG))
    }

    @Test
    fun testTrigonometryRad() {
        assertClose(0.0, eval("sin(0)", AngleMode.RAD))
        assertClose(1.0, eval("cos(0)", AngleMode.RAD))
        assertClose(0.0, eval("sin(π)", AngleMode.RAD))
    }

    @Test
    fun testConstants() {
        assertClose(Math.PI, eval("π"))
        assertClose(Math.E, eval("e"))
        assertClose(2 * Math.PI, eval("2π"))
    }

    @Test
    fun testRootsAndPowers() {
        assertClose(5.0, eval("sqrt(25)"))
        assertClose(5.0, eval("√25"))
        assertClose(3.0, eval("cbrt(27)"))
        assertClose(3.0, eval("∛27"))
        assertClose(16.0, eval("4^2"))
    }

    @Test
    fun testFactorialAndPercent() {
        assertClose(120.0, eval("5!"))
        assertClose(1.0, eval("0!"))
        assertClose(0.5, eval("50%"))
        assertClose(10.0, eval("100 * 10%"))
    }

    @Test
    fun testLogarithms() {
        assertClose(1.0, eval("ln(e)"))
        assertClose(2.0, eval("log(100)"))
        assertClose(3.0, eval("log2(8)"))
    }

    @Test
    fun testUnaryMinus() {
        assertClose(-5.0, eval("-5"))
        assertClose(-15.0, eval("3 * -5"))
        assertClose(2.0, eval("-3 + 5"))
        assertClose(-6.0, eval("-(2 + 4)"))
    }

    @Test
    fun testDivisionByZero() {
        val res = ExpressionEvaluator.evaluate("5 / 0", AngleMode.DEG)
        assertTrue(res is EvalResult.Error)
    }
}
