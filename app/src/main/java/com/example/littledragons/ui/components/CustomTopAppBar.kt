package com.example.littledragons.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = 65.dp,
    windowInsets: WindowInsets = WindowInsets.displayCutout,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { Title(title = title) },
        navigationIcon = navigationIcon,
        actions = actions,
        expandedHeight = expandedHeight,
        colors = getColors(),
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLargeTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    collapsedHeight: Dp = 65.dp,
    expandedHeight: Dp = 250.dp,
    windowInsets: WindowInsets = WindowInsets.displayCutout,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = { Title(title = title) },
        navigationIcon = navigationIcon,
        actions = actions,
        collapsedHeight = collapsedHeight,
        expandedHeight = expandedHeight,
        colors = getColors(),
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getColors(): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
    );
}

@Composable
private fun Title(title: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(start = 16.dp)
    ) {
        ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(lineHeight = 50.sp)) {
            title()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun CustomTopAppBarPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppPalette.GreenVariant1)) {
            CustomTopAppBar(
                title = { Text("Заголовок") },
                navigationIcon = {
                    BackButton(onClick = {})
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun CustomTopAppBarTitleOnlyPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppPalette.GreenVariant1)) {
            CustomTopAppBar(
                title = { Text("Заголовок") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun CustomLargeTopAppBarPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppPalette.GreenVariant1)) {
            CustomLargeTopAppBar(
                title = { Text("Заголовок") },
                navigationIcon = {
                    BackButton(onClick = {})
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }
            )
        }
    }
}
