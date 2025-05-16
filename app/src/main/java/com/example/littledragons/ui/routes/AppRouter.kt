package com.example.littledragons.ui.routes

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.littledragons.ui.InitialPage
import com.example.littledragons.ui.auth.LoginPage
import com.example.littledragons.ui.auth.RegisterPage
import com.example.littledragons.ui.events.EventsPage
import com.example.littledragons.ui.grades.GradesPage
import com.example.littledragons.ui.home.HomePage
import com.example.littledragons.ui.notifications.NotificationsPage
import com.example.littledragons.ui.profile.ProfilePage
import com.example.littledragons.ui.schedule.SchedulePage
import com.example.littledragons.ui.support.SupportPage

@Composable
fun AppRouter(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Анимация переходов
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val slideInHorizontally =
        slideInHorizontally(
            initialOffsetX = { if (isLtr) it else -it },
            animationSpec = tween(450)
        )
    val slideOutHorizontally =
        slideOutHorizontally(
            targetOffsetX = { if (isLtr) it else -it },
            animationSpec = tween(450)
        )
    val fadeIn = fadeIn(animationSpec = tween(150))
    val fadeOut = fadeOut(animationSpec = tween(400))

    NavHost(
        navController = navController,
        startDestination = AppDestination.Initial,
        enterTransition = { slideInHorizontally },
        popEnterTransition = { fadeIn },
        exitTransition = { fadeOut },
        popExitTransition = { slideOutHorizontally },
        modifier = modifier
    ) {
        // Перенаправление пользователя на страницу логина или главную страницу
        // в зависимости от авторизации
        composable<AppDestination.Initial> { InitialPage(navController = navController) }
        composable<AppDestination.Home> { HomePage(navController = navController) }
        composable<AppDestination.Profile> { ProfilePage(navController = navController) }
        composable<AppDestination.Events> { EventsPage(navController = navController) }
        composable<AppDestination.Schedules> { SchedulePage(navController = navController) }
        composable<AppDestination.Grades> { GradesPage(navController = navController) }
        composable<AppDestination.Notifications> { NotificationsPage(navController = navController) }
        composable<AppDestination.Support> { SupportPage(navController = navController) }
        authGraph(navController)
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AppDestination.Auth>(startDestination = AuthDestination.Login) {
        composable<AuthDestination.Login> { LoginPage(navController = navController) }
        composable<AuthDestination.Register> { RegisterPage(navController = navController) }
    }
}