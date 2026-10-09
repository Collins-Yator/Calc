package com.example.calculator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
    NUMBER,
    OPERATOR,
    EQUALS,
    ACTION,
    SCIENTIFIC,
    MEMORY
}

@Composable
fun CalculatorButton(
    text: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    subText: String? = null,
    isActive: Boolean = false,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val backgroundColor: Color
    val contentColor: Color

    when (type) {
        ButtonType.NUMBER -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.onSurface
        }
        ButtonType.OPERATOR -> {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        }
        ButtonType.EQUALS -> {
            backgroundColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.ACTION -> {
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        }
        ButtonType.SCIENTIFIC -> {
            backgroundColor = if (isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        }
        ButtonType.MEMORY -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val animatedBg by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 150),
        label = "btn_bg"
    )

    Surface(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                role = Role.Button,
                onClickLabel = contentDescription ?: text,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .testTag("btn_${text.filter { it.isLetterOrDigit() || it == '_' }.ifEmpty { "op" }}"),
        shape = RoundedCornerShape(22.dp),
        color = animatedBg,
        tonalElevation = if (type == ButtonType.NUMBER || type == ButtonType.EQUALS) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 8.dp)
                )
            }
            Text(
                text = text,
                fontSize = when {
                    text.length > 4 -> 15.sp
                    text.length > 2 -> 18.sp
                    type == ButtonType.EQUALS -> 28.sp
                    type == ButtonType.OPERATOR -> 24.sp
                    else -> 22.sp
                },
                fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
