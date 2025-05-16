package com.example.littledragons.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    Button(
        onClick = onClick,
        enabled = enabled,
        elevation = elevation,
        border = border,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = {
            ProvideTextStyle(
                TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 23.sp,
                    letterSpacing = 0.sp
                ),
            ) {
                content()
            }
        },
        modifier = modifier
            .background(
                Brush.linearGradient(
                    0.0f to AppPalette.Yellow,
                    0.6f to AppPalette.GreenVariant1,
                    start = Offset(-100f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                ),
                shape = shape
            )
            .height(56.dp),
    )
}

@Preview
@Composable
private fun CustomButtonPreview() {
    AppTheme {
        CustomButton(onClick = {}, modifier = Modifier.width(250.dp)) {
            Text("Текст")
        }
    }
}

@Preview
@Composable
private fun CustomButtonDisabledPreview() {
    AppTheme {
        CustomButton(onClick = {}, enabled = false, modifier = Modifier.width(250.dp)) {
            Text("Текст")
        }
    }
}

@Preview
@Composable
private fun CustomButtonProgressPreview() {
    AppTheme {
        CustomButton(
            onClick = {},
            enabled = false,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.width(250.dp)
        ) {
            CircularProgressIndicator()
        }
    }
}