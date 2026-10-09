package com.example.calculator.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.components.CalculatorDisplay
import com.example.calculator.ui.components.LandscapeKeypad
import com.example.calculator.ui.components.PortraitKeypad
import com.example.calculator.ui.history.HistorySheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Handle back button when history is open
    if (uiState.isHistoryOpen) {
        BackHandler {
            scope.launch {
                sheetState.hide()
                viewModel.toggleHistory(false)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CalcPro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Scientific Toggle (portrait only)
                    if (!isLandscape) {
                        IconButton(
                            onClick = { viewModel.toggleScientificExpanded() },
                            modifier = Modifier.testTag("btn_appbar_scientific")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Toggle scientific mode",
                                tint = if (uiState.isScientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // History Button with Badge
                    IconButton(
                        onClick = { viewModel.toggleHistory(true) },
                        modifier = Modifier.testTag("btn_appbar_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("${historyItems.size.coerceAtMost(99)}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation history",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 840.dp

            if (isWideScreen) {
                // Tablet Canonical Two-Pane Layout: Calculator on Left, Persistent History on Right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                    ) {
                        CalculatorDisplay(
                            expression = uiState.expression,
                            previewResult = uiState.previewResult,
                            lastEvaluation = uiState.lastEvaluation,
                            errorMessage = uiState.errorMessage,
                            angleMode = uiState.angleMode,
                            isSecondaryMode = uiState.isSecondaryMode,
                            hasMemory = uiState.hasMemory,
                            onAngleModeClick = { viewModel.toggleAngleMode() },
                            modifier = Modifier.weight(0.35f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LandscapeKeypad(
                            isSecondary = uiState.isSecondaryMode,
                            angleMode = uiState.angleMode,
                            hasMemory = uiState.hasMemory,
                            onDigit = { viewModel.onDigit(it) },
                            onOperator = { viewModel.onOperator(it) },
                            onFunction = { viewModel.onFunction(it) },
                            onConstant = { viewModel.onConstant(it) },
                            onParenthesis = { viewModel.onParenthesis(it) },
                            onDecimal = { viewModel.onDecimal() },
                            onPostfix = { viewModel.onPostfixOperator(it) },
                            onPowerTwo = { viewModel.onPowerTwo() },
                            onPowerThree = { viewModel.onPowerThree() },
                            onReciprocal = { viewModel.onReciprocal() },
                            onToggleSign = { viewModel.onToggleSign() },
                            onClear = { viewModel.onClear() },
                            onBackspace = { viewModel.onBackspace() },
                            onEquals = { viewModel.onEquals() },
                            onToggleSecondary = { viewModel.toggleSecondaryMode() },
                            onToggleAngleMode = { viewModel.toggleAngleMode() },
                            onMemoryAdd = { viewModel.onMemoryAdd() },
                            onMemorySubtract = { viewModel.onMemorySubtract() },
                            onMemoryRecall = { viewModel.onMemoryRecall() },
                            onMemoryClear = { viewModel.onMemoryClear() },
                            modifier = Modifier.weight(0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Persistent side history pane
                    HistorySheet(
                        historyList = historyItems,
                        searchQuery = uiState.historySearchQuery,
                        onlyFavorites = uiState.onlyFavorites,
                        onSearchChange = { viewModel.setHistorySearch(it) },
                        onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                        onClose = { viewModel.toggleHistory(false) },
                        onRecallExpression = { viewModel.onRecallExpression(it) },
                        onRecallResult = { viewModel.onRecallResult(it) },
                        onDelete = { viewModel.onDeleteHistoryItem(it) },
                        onClearAll = { viewModel.onClearAllHistory() },
                        onToggleFavorite = { id, fav -> viewModel.onToggleFavorite(id, fav) },
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                    )
                }
            } else if (isLandscape) {
                // Landscape phone layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    CalculatorDisplay(
                        expression = uiState.expression,
                        previewResult = uiState.previewResult,
                        lastEvaluation = uiState.lastEvaluation,
                        errorMessage = uiState.errorMessage,
                        angleMode = uiState.angleMode,
                        isSecondaryMode = uiState.isSecondaryMode,
                        hasMemory = uiState.hasMemory,
                        onAngleModeClick = { viewModel.toggleAngleMode() },
                        modifier = Modifier.weight(0.35f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LandscapeKeypad(
                        isSecondary = uiState.isSecondaryMode,
                        angleMode = uiState.angleMode,
                        hasMemory = uiState.hasMemory,
                        onDigit = { viewModel.onDigit(it) },
                        onOperator = { viewModel.onOperator(it) },
                        onFunction = { viewModel.onFunction(it) },
                        onConstant = { viewModel.onConstant(it) },
                        onParenthesis = { viewModel.onParenthesis(it) },
                        onDecimal = { viewModel.onDecimal() },
                        onPostfix = { viewModel.onPostfixOperator(it) },
                        onPowerTwo = { viewModel.onPowerTwo() },
                        onPowerThree = { viewModel.onPowerThree() },
                        onReciprocal = { viewModel.onReciprocal() },
                        onToggleSign = { viewModel.onToggleSign() },
                        onClear = { viewModel.onClear() },
                        onBackspace = { viewModel.onBackspace() },
                        onEquals = { viewModel.onEquals() },
                        onToggleSecondary = { viewModel.toggleSecondaryMode() },
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        onMemoryAdd = { viewModel.onMemoryAdd() },
                        onMemorySubtract = { viewModel.onMemorySubtract() },
                        onMemoryRecall = { viewModel.onMemoryRecall() },
                        onMemoryClear = { viewModel.onMemoryClear() },
                        modifier = Modifier.weight(0.65f)
                    )
                }
            } else {
                // Portrait phone layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    CalculatorDisplay(
                        expression = uiState.expression,
                        previewResult = uiState.previewResult,
                        lastEvaluation = uiState.lastEvaluation,
                        errorMessage = uiState.errorMessage,
                        angleMode = uiState.angleMode,
                        isSecondaryMode = uiState.isSecondaryMode,
                        hasMemory = uiState.hasMemory,
                        onAngleModeClick = { viewModel.toggleAngleMode() },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PortraitKeypad(
                        isSecondary = uiState.isSecondaryMode,
                        isExpanded = uiState.isScientificExpanded,
                        angleMode = uiState.angleMode,
                        hasMemory = uiState.hasMemory,
                        onDigit = { viewModel.onDigit(it) },
                        onOperator = { viewModel.onOperator(it) },
                        onFunction = { viewModel.onFunction(it) },
                        onConstant = { viewModel.onConstant(it) },
                        onParenthesis = { viewModel.onParenthesis(it) },
                        onDecimal = { viewModel.onDecimal() },
                        onPostfix = { viewModel.onPostfixOperator(it) },
                        onPowerTwo = { viewModel.onPowerTwo() },
                        onPowerThree = { viewModel.onPowerThree() },
                        onReciprocal = { viewModel.onReciprocal() },
                        onToggleSign = { viewModel.onToggleSign() },
                        onClear = { viewModel.onClear() },
                        onBackspace = { viewModel.onBackspace() },
                        onEquals = { viewModel.onEquals() },
                        onToggleSecondary = { viewModel.toggleSecondaryMode() },
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        onToggleExpand = { viewModel.toggleScientificExpanded() },
                        onMemoryAdd = { viewModel.onMemoryAdd() },
                        onMemorySubtract = { viewModel.onMemorySubtract() },
                        onMemoryRecall = { viewModel.onMemoryRecall() },
                        onMemoryClear = { viewModel.onMemoryClear() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // BottomSheet for History in portrait/compact mode
            if (uiState.isHistoryOpen && !isWideScreen) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleHistory(false) },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    dragHandle = null
                ) {
                    HistorySheet(
                        historyList = historyItems,
                        searchQuery = uiState.historySearchQuery,
                        onlyFavorites = uiState.onlyFavorites,
                        onSearchChange = { viewModel.setHistorySearch(it) },
                        onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                        onClose = {
                            scope.launch {
                                sheetState.hide()
                                viewModel.toggleHistory(false)
                            }
                        },
                        onRecallExpression = {
                            viewModel.onRecallExpression(it)
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        onRecallResult = {
                            viewModel.onRecallResult(it)
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        onDelete = { viewModel.onDeleteHistoryItem(it) },
                        onClearAll = { viewModel.onClearAllHistory() },
                        onToggleFavorite = { id, fav -> viewModel.onToggleFavorite(id, fav) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    )
                }
            }
        }
    }
}
