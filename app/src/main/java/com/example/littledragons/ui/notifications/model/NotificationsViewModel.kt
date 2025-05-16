package com.example.littledragons.ui.notifications.model.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.NotificationsRepository
import com.example.littledragons.model.types.Notification
import com.example.littledragons.ui.notifications.model.NotificationsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface INotificationsViewModel {
    val state: StateFlow<NotificationsState>
    val deleteResult: StateFlow<DeleteNotificationResult>
    fun load(force: Boolean = false)
    fun delete(notification: Notification)
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepo: NotificationsRepository,
) : ViewModel(), INotificationsViewModel {
    val _state = MutableStateFlow<NotificationsState>(NotificationsState.Initial)
    override val state: StateFlow<NotificationsState> = _state

    val _deleteResult = MutableStateFlow<DeleteNotificationResult>(DeleteNotificationResult.Initial)
    override val deleteResult: StateFlow<DeleteNotificationResult> = _deleteResult

    override fun load(force: Boolean) {
        if (!force && (state.value == NotificationsState.Loading || state.value is NotificationsState.Loaded)) {
            return
        }

        viewModelScope.launch {
            _state.emit(NotificationsState.Loading)

            try {
                _state.emit(
                    NotificationsState.Loaded(notificationsRepo.getAll())
                )
            } catch (e: Throwable) {
                Log.e(
                    "NotificationsViewModel",
                    "Unable to fetch notifications list",
                    e
                )
                _state.emit(NotificationsState.Failed(e))
            }
        }
    }

    override fun delete(notification: Notification) {
        viewModelScope.launch {
            _deleteResult.emit(DeleteNotificationResult.InProgress)

            try {
                removeFromList(notification)
                notificationsRepo.delete(notification)
                _deleteResult.emit(DeleteNotificationResult.Success)
            } catch (e: Throwable) {
                Log.e(
                    "NotificationsViewModel",
                    "Unable to delete notification",
                    e
                )
                returnToList(notification)
                _deleteResult.emit(DeleteNotificationResult.Failed(e))
            }
        }
    }

    private suspend fun removeFromList(event: Notification) {
        when (val s = _state.value) {
            is NotificationsState.Loaded -> _state.emit(NotificationsState.Loaded(s.notifications.filter { event != it }))
            else -> {}
        }
    }

    private suspend fun returnToList(event: Notification) {
        when (val s = _state.value) {
            is NotificationsState.Loaded -> _state.emit(
                NotificationsState.Loaded(
                    (s.notifications + event).sortedByDescending { it.timestamp })
            )

            else -> {}
        }
    }
}