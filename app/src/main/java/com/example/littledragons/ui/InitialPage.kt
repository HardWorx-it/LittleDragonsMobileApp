package com.example.littledragons.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.ui.model.InitialViewModel
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.theme.AppTheme

/**
 * Перенаправление пользователя на страницу логина или главную страницу
 * в зависимости от авторизации
 */
@Composable
fun InitialPage(
    viewModel: InitialViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(startDestination) {
        if (startDestination != null) {
            navController.navigate(startDestination!!) {
                popUpTo(AppDestination.Initial) {
                    inclusive = true
                }
            }
        }
    }

    Content()
}

@Composable
private fun Content() {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun ContentPreview() {
    AppTheme {
        Content()
    }
}