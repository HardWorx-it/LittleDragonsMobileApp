package com.example.littledragons.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme

@Composable
fun CustomScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    0.0f to AppPalette.GreenVariant1,
                    1.0f to AppPalette.GreenVariant2,
                    end = Offset(900.0f, 900.0f)
                )
            )
    ) {
        Image(
            painterResource(R.drawable.star_pattern),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier
                .safeDrawingPadding()
                .padding(top = 16.dp)
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            contentWindowInsets = contentWindowInsets,
            modifier = modifier,
        ) { contentPadding ->
            Surface(
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                val direction = LocalLayoutDirection.current;
                val newContentPadding = PaddingValues(
                    top = 22.dp,
                    start = contentPadding.calculateStartPadding(direction),
                    end = contentPadding.calculateEndPadding(direction),
                    bottom = contentPadding.calculateBottomPadding(),
                )
                Box(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.displayCutout.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
                ) {
                    content(newContentPadding)
                }
            }
        }
        BottomBarBackground()
    }
}

@Composable
private fun BoxScope.BottomBarBackground() {
    val density = LocalDensity.current;
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .height(
                with(density) { WindowInsets.systemBars.getBottom(this).toDp() }
            )
            .background(MaterialTheme.colorScheme.background)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun CustomScaffoldPreview() {
    AppTheme {
        CustomScaffold(
            topBar = {
                CustomTopAppBar(title = {
                    Text("Заголовок")
                })
            }
        ) {}
    }
}