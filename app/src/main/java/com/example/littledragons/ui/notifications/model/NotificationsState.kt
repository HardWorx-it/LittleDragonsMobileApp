package com.example.littledragons.ui.notifications.model

import com.example.littledragons.model.types.Notification

sealed interface NotificationsState {
    data object Initial : NotificationsState
    data object Loading : NotificationsState
    data class Loaded(val notifications: List<Notification>) : NotificationsState
    data class Failed(val error: Throwable) : NotificationsState
}
