package com.example.littledragons.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import com.example.littledragons.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExposedDropdownMenu(
    modifier: Modifier = Modifier,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
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
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            supportingText = supportingText,
            singleLine = singleLine,
            trailingIcon = trailingIcon ?: {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            maxLines = maxLines,
            minLines = minLines,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        val filteringOptions = options.filter { it.contains(value, ignoreCase = true) }
        if (filteringOptions.isNotEmpty()) {
            DropdownMenu(
                modifier = Modifier.exposedDropdownSize(true),
                properties = PopupProperties(focusable = false),
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
            ) {
                for (option in options) {
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onValueChange(option)
                            onExpandedChange(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CustomExposedDropdownMenuPreview() {
    AppTheme {
        CustomExposedDropdownMenu(
            options = listOf("Текст1", "Текст2", "Текст3"),
            value = "Текст", onValueChange = {},
            label = { Text("Надпись") },
            expanded = false,
            onExpandedChange = {}
        )
    }
}

@Preview
@Composable
private fun CustomExposedDropdownMenuEmptyPreview() {
    AppTheme {
        CustomExposedDropdownMenu(
            options = listOf("Текст1", "Текст2", "Текст3"),
            value = "", onValueChange = {},
            label = { Text("Надпись") },
            expanded = false,
            onExpandedChange = {}
        )
    }
}