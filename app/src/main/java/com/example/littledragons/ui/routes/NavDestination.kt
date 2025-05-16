package com.example.littledragons.ui.routes

import kotlinx.serialization.Serializable

interface Destination

@Serializable
sealed class AppDestination : Destination {
    @Serializable
    data object Initial : AppDestination()

    @Serializable
    data object Home : AppDestination()

    @Serializable
    data object Auth : AppDestination()

    @Serializable
    data object Profile : AppDestination()

    @Serializable
    data object Events : AppDestination()

    @Serializable
    data object Schedules : AppDestination()

    @Serializable
    data object Grades : AppDestination()

    @Serializable
    data object Notifications : AppDestination()

    @Serializable
    data object Support : AppDestination()
}

@Serializable
sealed class AuthDestination : Destination {
    @Serializable
    data object Login : AppDestination()

    @Serializable
    data object Register : AppDestination()
}