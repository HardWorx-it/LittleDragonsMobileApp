package com.example.littledragons.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val indicatorColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val height = 46.dp
    val colors = TextFieldDefaults.colors(
        unfocusedContainerColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = indicatorColor,
        unfocusedIndicatorColor = indicatorColor,
        disabledIndicatorColor = indicatorColor,
        focusedLabelColor = labelColor,
        unfocusedLabelColor = labelColor,
        disabledLabelColor = labelColor,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = height
            ),
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            label = label,
            supportingText = supportingText,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            innerTextField = {
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    innerTextField()
                }
            },
            singleLine = singleLine,
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = if (label == null) {
                TextFieldDefaults.contentPaddingWithoutLabel(
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 18.dp
                )
            } else {
                TextFieldDefaults.contentPaddingWithLabel(
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 18.dp
                )
            },
            isError = isError,
            colors = colors,
        )
    }
}

@Preview
@Composable
private fun CustomTextFieldPreview() {
    AppTheme {
        CustomTextField(
            value = "Текст", onValueChange = {},
            label = { Text("Надпись") },
        )
    }
}

@Preview
@Composable
private fun CustomTextFieldEmptyPreview() {
    AppTheme {
        CustomTextField(
            value = "", onValueChange = {},
            label = { Text("Надпись") },
        )
    }
}

@Preview
@Composable
private fun CustomTextFieldErrorPreview() {
    AppTheme {
        CustomTextField(
            value = "Текст", onValueChange = {},
            label = { Text("Надпись") },
            supportingText = { Text("Ошибка") },
            isError = true
        )
    }
}