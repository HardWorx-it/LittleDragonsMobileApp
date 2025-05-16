package com.example.littledragons.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import com.example.littledragons.ui.routes.AppDestination
import java.text.DateFormatSymbols
import java.util.Locale

val customPasswordVisualTransformation = PasswordVisualTransformation('\u25CF')

fun NavController.navigateToRedirectAuth() = navigate(AppDestination.Initial) {
    popUpTo(graph.startDestinationId) {
        inclusive = true
    }
}

fun getMonthNames(languageTag: String): List<String> {
    val locale = Locale.forLanguageTag(languageTag)
    val symbols = DateFormatSymbols.getInstance(locale)
    return symbols.months.take(12)
}

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM {
    val context = LocalContext.current
    val viewModelStoreOwner = context as? ViewModelStoreOwner
        ?: error("Context is not a ViewModelStoreOwner")
    return hiltViewModel(viewModelStoreOwner)
}