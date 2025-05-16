package com.example.littledragons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.routes.AppRouter
import com.example.littledragons.ui.theme.AppTheme

@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = activityViewModel(),
) {
    val navController = rememberNavController();

    LaunchedEffect(true) {
        viewModel.init()
    }

    AppTheme {
        AppRouter(
            navController,
            modifier
        )
    }
}