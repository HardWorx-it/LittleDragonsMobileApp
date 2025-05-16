package com.example.littledragons.ui.events.model

sealed class AddEventState {
    data object Initial : AddEventState()
    data object InProgress : AddEventState()
    data object Success : AddEventState()
    data class Failed(val error: Throwable) : AddEventState()
}