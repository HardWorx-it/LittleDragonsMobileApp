package com.example.littledragons.ui.notifications.model

sealed class AddNotificationState {
    data object Initial : AddNotificationState()
    data object InProgress : AddNotificationState()
    data object Success : AddNotificationState()
    data class Failed(val error: Throwable) : AddNotificationState()
}