package com.example.littledragons.ui.events.model

sealed interface DeleteEventResult {
    data object Initial : DeleteEventResult
    data object InProgress : DeleteEventResult
    data object Success : DeleteEventResult
    data class Failed(val error: Throwable) : DeleteEventResult
}