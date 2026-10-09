package com.example.calculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.CalculationItem
import com.example.calculator.data.CalculationRepository
import com.example.calculator.data.CalculatorDatabase
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.EvalResult
import com.example.calculator.engine.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String? = null,
    val lastResult: String? = null,
    val lastEvaluation: String? = null,
    val isEvaluated: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val isSecondaryMode: Boolean = false,
    val isScientificExpanded: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val errorMessage: String? = null,
    val isHistoryOpen: Boolean = false,
    val historySearchQuery: String = "",
    val onlyFavorites: Boolean = false
)

class CalculatorViewModel(
    application: Application,
    private val repository: CalculationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val historyItems: StateFlow<List<CalculationItem>> = combine(
        repository.history,
        _uiState
    ) { list, state ->
        var filtered = list
        if (state.onlyFavorites) {
            filtered = filtered.filter { it.isFavorite }
        }
        if (state.historySearchQuery.isNotBlank()) {
            val q = state.historySearchQuery.trim().lowercase()
            filtered = filtered.filter {
                it.expression.lowercase().contains(q) || it.result.lowercase().contains(q)
            }
        }
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val newExpr = if (state.isEvaluated) {
                digit
            } else {
                state.expression + digit
            }
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "0." else state.expression
            val newExpr = if (expr.isEmpty() || expr.last() in "+-×*÷/^(") {
                expr + "0."
            } else {
                // Check if current number segment already has a decimal point
                val lastNum = expr.takeLastWhile { it.isDigit() || it == '.' }
                if (lastNum.contains('.')) expr else expr + "."
            }
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            var expr = state.expression
            if (state.isEvaluated && state.lastResult != null) {
                // Chain from previous result
                expr = state.lastResult.replace(",", "")
            }

            if (expr.isEmpty()) {
                if (op == "-" || op == "−") {
                    return@update state.copy(
                        expression = "-",
                        isEvaluated = false,
                        errorMessage = null
                    )
                }
                return@update state
            }

            // If last char is already an operator, replace it
            val lastChar = expr.last()
            val newExpr = if (lastChar in "+-×*÷/–−^") {
                expr.dropLast(1) + op
            } else {
                expr + op
            }

            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onFunction(functionName: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = "$expr$functionName("
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = expr + constant
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onParenthesis(paren: String) {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = expr + paren
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onSmartParenthesis() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val nextParen = if (expr.isEmpty() || expr.last() in "+-×*÷/^(") {
                "("
            } else if (openCount > closeCount) {
                ")"
            } else {
                "("
            }

            val newExpr = expr + nextParen
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onPostfixOperator(symbol: String) {
        // e.g., '!' or '%'
        _uiState.update { state ->
            val expr = if (state.isEvaluated && state.lastResult != null) {
                state.lastResult.replace(",", "")
            } else {
                state.expression
            }
            if (expr.isEmpty() || expr.last() in "+-×*÷/^(") {
                return@update state
            }
            val newExpr = expr + symbol
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onPowerTwo() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated && state.lastResult != null) {
                state.lastResult.replace(",", "")
            } else {
                state.expression
            }
            if (expr.isEmpty() || expr.last() in "+-×*÷/^(") return@update state
            val newExpr = "$expr^2"
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onPowerThree() {
        _uiState.update { state ->
            val expr = if (state.isEvaluated && state.lastResult != null) {
                state.lastResult.replace(",", "")
            } else {
                state.expression
            }
            if (expr.isEmpty() || expr.last() in "+-×*÷/^(") return@update state
            val newExpr = "$expr^3"
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onReciprocal() {
        // 1/x -> wrapped into (1/(expr)) or 1/(
        _uiState.update { state ->
            val expr = if (state.isEvaluated && state.lastResult != null) {
                state.lastResult.replace(",", "")
            } else {
                state.expression
            }
            val newExpr = if (expr.isEmpty()) {
                "1/("
            } else {
                "1/($expr)"
            }
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) {
                return@update state.copy(expression = "-")
            }

            // Find last number or token
            val newExpr = if (expr.startsWith("-(") && expr.endsWith(")")) {
                expr.substring(2, expr.length - 1)
            } else if (expr.startsWith("-") && expr.drop(1).toDoubleOrNull() != null) {
                expr.drop(1)
            } else if (expr.toDoubleOrNull() != null) {
                "-$expr"
            } else {
                "-($expr)"
            }

            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.isEvaluated) {
                return@update state.copy(
                    expression = "",
                    isEvaluated = false,
                    previewResult = null,
                    errorMessage = null
                )
            }

            val expr = state.expression
            if (expr.isEmpty()) return@update state

            // Check if ending with function name like "sin(", "asin(", "cosh(", "log(", "sqrt("
            val multiCharTokens = listOf(
                "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(",
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(",
                "sin(", "cos(", "tan(", "log₁₀(", "log(", "ln(", "exp(", "abs(",
                "sqrt(", "cbrt(", "√(", "∛("
            )

            var matchedToken: String? = null
            for (token in multiCharTokens) {
                if (expr.endsWith(token)) {
                    matchedToken = token
                    break
                }
            }

            val newExpr = if (matchedToken != null) {
                expr.dropLast(matchedToken.length)
            } else {
                expr.dropLast(1)
            }

            state.copy(
                expression = newExpr,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(newExpr, state.angleMode)
            )
        }
    }

    fun onClear() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                previewResult = null,
                errorMessage = null,
                isEvaluated = false
            )
        }
    }

    fun onEquals() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        when (val eval = ExpressionEvaluator.evaluate(expr, state.angleMode)) {
            is EvalResult.Success -> {
                val formatted = eval.formatted
                _uiState.update {
                    it.copy(
                        lastEvaluation = expr,
                        lastResult = formatted,
                        previewResult = null,
                        expression = formatted,
                        isEvaluated = true,
                        errorMessage = null
                    )
                }

                // Save to Room database history
                viewModelScope.launch {
                    repository.saveCalculation(
                        CalculationItem(
                            expression = expr,
                            result = formatted,
                            numericResult = eval.value,
                            angleMode = state.angleMode.name
                        )
                    )
                }
            }
            is EvalResult.Error -> {
                _uiState.update {
                    it.copy(errorMessage = eval.message)
                }
            }
        }
    }

    fun toggleAngleMode() {
        _uiState.update { state ->
            val newMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            state.copy(
                angleMode = newMode,
                previewResult = ExpressionEvaluator.preview(state.expression, newMode)
            )
        }
    }

    fun toggleSecondaryMode() {
        _uiState.update { it.copy(isSecondaryMode = !it.isSecondaryMode) }
    }

    fun toggleScientificExpanded() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleHistory(open: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = open) }
    }

    fun setHistorySearch(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun toggleFavoritesOnly() {
        _uiState.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    fun onRecallExpression(expression: String) {
        _uiState.update { state ->
            state.copy(
                expression = expression,
                isEvaluated = false,
                isHistoryOpen = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(expression, state.angleMode)
            )
        }
    }

    fun onRecallResult(result: String) {
        _uiState.update { state ->
            val cleanResult = result.replace(",", "")
            val expr = if (state.isEvaluated) cleanResult else state.expression + cleanResult
            state.copy(
                expression = expr,
                isEvaluated = false,
                isHistoryOpen = false,
                errorMessage = null,
                previewResult = ExpressionEvaluator.preview(expr, state.angleMode)
            )
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCalculation(id)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun onToggleFavorite(id: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, !currentFavorite)
        }
    }

    // Memory operations (MC, MR, M+, M-)
    fun onMemoryClear() {
        _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    fun onMemoryRecall() {
        val mem = _uiState.value.memoryValue
        val memFormatted = ExpressionEvaluator.formatResult(mem).replace(",", "")
        _uiState.update { state ->
            val expr = if (state.isEvaluated) memFormatted else state.expression + memFormatted
            state.copy(
                expression = expr,
                isEvaluated = false,
                previewResult = ExpressionEvaluator.preview(expr, state.angleMode)
            )
        }
    }

    fun onMemoryAdd() {
        val state = _uiState.value
        val target = state.lastResult ?: state.previewResult ?: state.expression
        val eval = ExpressionEvaluator.evaluate(target, state.angleMode)
        if (eval is EvalResult.Success) {
            val newMem = state.memoryValue + eval.value
            _uiState.update { it.copy(memoryValue = newMem, hasMemory = abs(newMem) > 1e-12) }
        }
    }

    fun onMemorySubtract() {
        val state = _uiState.value
        val target = state.lastResult ?: state.previewResult ?: state.expression
        val eval = ExpressionEvaluator.evaluate(target, state.angleMode)
        if (eval is EvalResult.Success) {
            val newMem = state.memoryValue - eval.value
            _uiState.update { it.copy(memoryValue = newMem, hasMemory = abs(newMem) > 1e-12) }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CalculatorDatabase.getInstance(application)
                    val repository = CalculationRepository(db.calculationDao())
                    return CalculatorViewModel(application, repository) as T
                }
            }
        }
    }
}
