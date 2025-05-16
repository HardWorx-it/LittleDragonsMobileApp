package com.example.littledragons.ui.support

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.theme.AppTheme

@Composable
fun SupportPage(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Page(
        onBack = navController::navigateUp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    CustomScaffold(
        topBar = {
            CustomTopAppBar(
                title = { Text(stringResource(R.string.support)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(
                    top = contentPadding.calculateTopPadding() + 12.dp,
                    end = 24.dp,
                    start = 24.dp
                )
                .fillMaxSize()
        ) {
            Text(
                AnnotatedString.fromHtml(
                    stringResource(R.string.support_link),
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                        )
                    )
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun SupportPagePreview() {
    AppTheme {
        Page(
            onBack = {}
        )
    }
}