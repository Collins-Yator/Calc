package com.example.calculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode

@Composable
fun PortraitKeypad(
    isSecondary: Boolean,
    isExpanded: Boolean,
    angleMode: AngleMode,
    hasMemory: Boolean,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onDecimal: () -> Unit,
    onPostfix: (String) -> Unit,
    onPowerTwo: () -> Unit,
    onPowerThree: () -> Unit,
    onReciprocal: () -> Unit,
    onToggleSign: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleSecondary: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleExpand: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Quick Scientific Strip (Always visible, horizontally scrollable or expandable toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary 2nd toggle chip
                AssistChip(
                    onClick = onToggleSecondary,
                    label = { Text("2nd", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSecondary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        labelColor = if (isSecondary) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null,
                    modifier = Modifier.height(34.dp).testTag("chip_secondary")
                )

                // Quick functions
                val quickTrig1 = if (isSecondary) "sin⁻¹" else "sin"
                val quickTrig2 = if (isSecondary) "cos⁻¹" else "cos"
                val quickTrig3 = if (isSecondary) "tan⁻¹" else "tan"
                val quickLog = if (isSecondary) "10ˣ" else "log"
                val quickLn = if (isSecondary) "eˣ" else "ln"

                QuickChip(text = quickTrig1) { onFunction(quickTrig1) }
                QuickChip(text = quickTrig2) { onFunction(quickTrig2) }
                QuickChip(text = quickTrig3) { onFunction(quickTrig3) }
                QuickChip(text = quickLn) { if (isSecondary) onFunction("exp") else onFunction("ln") }
                QuickChip(text = quickLog) { if (isSecondary) onFunction("10^") else onFunction("log") }
                QuickChip(text = "√") { onFunction("sqrt") }
                QuickChip(text = "^") { onOperator("^") }
                QuickChip(text = "π") { onConstant("π") }
                QuickChip(text = "e") { onConstant("e") }
                QuickChip(text = "n!") { onPostfix("!") }
                QuickChip(text = "%") { onPostfix("%") }
            }

            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.testTag("btn_toggle_scientific_expand")
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse scientific panel" else "Expand scientific panel",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Expanded Scientific Tray
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Row S1: Hyperbolic & Powers
                Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    val sinhTxt = if (isSecondary) "sinh⁻¹" else "sinh"
                    val coshTxt = if (isSecondary) "cosh⁻¹" else "cosh"
                    val tanhTxt = if (isSecondary) "tanh⁻¹" else "tanh"

                    CalculatorButton(text = sinhTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(sinhTxt) }
                    CalculatorButton(text = coshTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(coshTxt) }
                    CalculatorButton(text = tanhTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(tanhTxt) }
                    CalculatorButton(text = "x²", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPowerTwo() }
                    CalculatorButton(text = "x³", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPowerThree() }
                }

                // Row S2: Roots, Inverses, Absolute, Reciprocal
                Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    CalculatorButton(text = "∛", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("cbrt") }
                    CalculatorButton(text = "1/x", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onReciprocal() }
                    CalculatorButton(text = "|x|", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("abs") }
                    CalculatorButton(text = "log₂", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("log2") }
                    CalculatorButton(text = "MC", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryClear() }
                }

                // Row S3: Memory keys
                Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    CalculatorButton(text = "MR", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryRecall() }
                    CalculatorButton(text = "M+", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryAdd() }
                    CalculatorButton(text = "M-", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemorySubtract() }
                    CalculatorButton(text = "π", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onConstant("π") }
                    CalculatorButton(text = "e", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onConstant("e") }
                }
            }
        }

        // Standard Keypad Grid (4 columns x 5 rows)
        // Row 1: AC, (, ), ÷
        Row(modifier = Modifier.fillMaxWidth().height(62.dp)) {
            CalculatorButton(text = "AC", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onClear() }
            CalculatorButton(text = "(", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onParenthesis("(") }
            CalculatorButton(text = ")", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onParenthesis(")") }
            CalculatorButton(text = "÷", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("÷") }
        }

        // Row 2: 7, 8, 9, ×
        Row(modifier = Modifier.fillMaxWidth().height(62.dp)) {
            CalculatorButton(text = "7", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("7") }
            CalculatorButton(text = "8", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("8") }
            CalculatorButton(text = "9", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("9") }
            CalculatorButton(text = "×", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("×") }
        }

        // Row 3: 4, 5, 6, −
        Row(modifier = Modifier.fillMaxWidth().height(62.dp)) {
            CalculatorButton(text = "4", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("4") }
            CalculatorButton(text = "5", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("5") }
            CalculatorButton(text = "6", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("6") }
            CalculatorButton(text = "−", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("−") }
        }

        // Row 4: 1, 2, 3, +
        Row(modifier = Modifier.fillMaxWidth().height(62.dp)) {
            CalculatorButton(text = "1", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("1") }
            CalculatorButton(text = "2", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("2") }
            CalculatorButton(text = "3", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("3") }
            CalculatorButton(text = "+", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("+") }
        }

        // Row 5: 0, ., ⌫, =
        Row(modifier = Modifier.fillMaxWidth().height(62.dp)) {
            CalculatorButton(text = "0", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("0") }
            CalculatorButton(text = ".", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDecimal() }
            CalculatorButton(text = "⌫", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onBackspace() }
            CalculatorButton(text = "=", type = ButtonType.EQUALS, modifier = Modifier.weight(1f)) { onEquals() }
        }
    }
}

@Composable
fun LandscapeKeypad(
    isSecondary: Boolean,
    angleMode: AngleMode,
    hasMemory: Boolean,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onDecimal: () -> Unit,
    onPostfix: (String) -> Unit,
    onPowerTwo: () -> Unit,
    onPowerThree: () -> Unit,
    onReciprocal: () -> Unit,
    onToggleSign: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleSecondary: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Column: Scientific functions (5 columns x 5 rows)
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val sinTxt = if (isSecondary) "sin⁻¹" else "sin"
            val cosTxt = if (isSecondary) "cos⁻¹" else "cos"
            val tanTxt = if (isSecondary) "tan⁻¹" else "tan"
            val lnTxt = if (isSecondary) "eˣ" else "ln"
            val logTxt = if (isSecondary) "10ˣ" else "log"

            // Row 1
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "2nd", type = ButtonType.SCIENTIFIC, isActive = isSecondary, modifier = Modifier.weight(1f)) { onToggleSecondary() }
                CalculatorButton(text = angleMode.name, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onToggleAngleMode() }
                CalculatorButton(text = sinTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(sinTxt) }
                CalculatorButton(text = cosTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(cosTxt) }
                CalculatorButton(text = tanTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(tanTxt) }
            }

            // Row 2
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                val sinhTxt = if (isSecondary) "sinh⁻¹" else "sinh"
                val coshTxt = if (isSecondary) "cosh⁻¹" else "cosh"
                val tanhTxt = if (isSecondary) "tanh⁻¹" else "tanh"
                CalculatorButton(text = sinhTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(sinhTxt) }
                CalculatorButton(text = coshTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(coshTxt) }
                CalculatorButton(text = tanhTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction(tanhTxt) }
                CalculatorButton(text = lnTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { if (isSecondary) onFunction("exp") else onFunction("ln") }
                CalculatorButton(text = logTxt, type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { if (isSecondary) onFunction("10^") else onFunction("log") }
            }

            // Row 3
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "x²", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPowerTwo() }
                CalculatorButton(text = "x³", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPowerThree() }
                CalculatorButton(text = "^", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onOperator("^") }
                CalculatorButton(text = "√", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("sqrt") }
                CalculatorButton(text = "∛", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("cbrt") }
            }

            // Row 4
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "1/x", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onReciprocal() }
                CalculatorButton(text = "|x|", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onFunction("abs") }
                CalculatorButton(text = "n!", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPostfix("!") }
                CalculatorButton(text = "%", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onPostfix("%") }
                CalculatorButton(text = "π", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onConstant("π") }
            }

            // Row 5
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "e", type = ButtonType.SCIENTIFIC, modifier = Modifier.weight(1f)) { onConstant("e") }
                CalculatorButton(text = "MC", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryClear() }
                CalculatorButton(text = "MR", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryRecall() }
                CalculatorButton(text = "M+", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemoryAdd() }
                CalculatorButton(text = "M-", type = ButtonType.MEMORY, modifier = Modifier.weight(1f)) { onMemorySubtract() }
            }
        }

        // Right Column: Basic Arithmetic keypad (4 columns x 5 rows)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "AC", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onClear() }
                CalculatorButton(text = "(", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onParenthesis("(") }
                CalculatorButton(text = ")", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onParenthesis(")") }
                CalculatorButton(text = "÷", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("÷") }
            }
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "7", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("7") }
                CalculatorButton(text = "8", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("8") }
                CalculatorButton(text = "9", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("9") }
                CalculatorButton(text = "×", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("×") }
            }
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "4", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("4") }
                CalculatorButton(text = "5", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("5") }
                CalculatorButton(text = "6", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("6") }
                CalculatorButton(text = "−", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("−") }
            }
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "1", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("1") }
                CalculatorButton(text = "2", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("2") }
                CalculatorButton(text = "3", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("3") }
                CalculatorButton(text = "+", type = ButtonType.OPERATOR, modifier = Modifier.weight(1f)) { onOperator("+") }
            }
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                CalculatorButton(text = "0", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDigit("0") }
                CalculatorButton(text = ".", type = ButtonType.NUMBER, modifier = Modifier.weight(1f)) { onDecimal() }
                CalculatorButton(text = "⌫", type = ButtonType.ACTION, modifier = Modifier.weight(1f)) { onBackspace() }
                CalculatorButton(text = "=", type = ButtonType.EQUALS, modifier = Modifier.weight(1f)) { onEquals() }
            }
        }
    }
}

@Composable
private fun QuickChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.height(34.dp)
    ) {
        AssistChip(
            onClick = onClick,
            label = { Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = null,
            modifier = Modifier.height(34.dp).testTag("quick_$text")
        )
    }
}
