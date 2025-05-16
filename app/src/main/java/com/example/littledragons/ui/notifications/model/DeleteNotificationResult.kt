package com.example.littledragons.ui.notifications.model.model

sealed interface DeleteNotificationResult {
    data object Initial : DeleteNotificationResult
    data object InProgress : DeleteNotificationResult
    data object Success : DeleteNotificationResult
    data class Failed(val error: Throwable) : DeleteNotificationResult
}