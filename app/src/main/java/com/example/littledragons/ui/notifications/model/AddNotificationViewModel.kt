package com.example.littledragons.ui.notifications.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.NotificationsRepository
import com.example.littledragons.model.types.Notification
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IAddNotificationViewModel {
    val name: String
    val nameIsNotValid: Boolean
    val refresh: SharedFlow<Boolean>
    val state: StateFlow<AddNotificationState>
    fun updateName(value: String)
    fun submit(timestamp: Timestamp)
    fun clear()
}

@HiltViewModel
class AddNotificationViewModel @Inject constructor(
    private val notificationsRepo: NotificationsRepository
) : ViewModel(), IAddNotificationViewModel {
    override var name: String by mutableStateOf("")
        private set
    override var nameIsNotValid: Boolean by mutableStateOf(false)
        private set

    val _state = MutableStateFlow<AddNotificationState>(AddNotificationState.Initial)
    override val state: StateFlow<AddNotificationState> = _state

    val _refresh = MutableSharedFlow<Boolean>()
    override val refresh: SharedFlow<Boolean> = _refresh

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun submit(timestamp: Timestamp) {
        if (state == NotificationsState.Loading) {
            return
        }

        name = name.trim()
        nameIsNotValid = name.isEmpty()
        if (nameIsNotValid) {
            return
        }

        viewModelScope.launch {
            try {
                _state.emit(AddNotificationState.InProgress)
                _refresh.emit(false)

                val notification = Notification(
                    title = name,
                    timestamp = timestamp,
                )
                notificationsRepo.add(notification)
                clear()
                _state.emit(AddNotificationState.Success)
                _refresh.emit(true)
            } catch (e: Throwable) {
                Log.e(
                    "AddNotificationViewModel",
                    "Unable to add or update notification",
                    e
                )
                _state.emit(AddNotificationState.Failed(e))
            }
        }
    }

    override fun clear() {
        name = ""
        nameIsNotValid = false
    }
}