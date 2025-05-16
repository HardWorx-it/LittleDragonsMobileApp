package com.example.littledragons.ui.schedule.model

sealed class AddScheduleState {
    data object Initial : AddScheduleState()
    data object InProgress : AddScheduleState()
    data object Success : AddScheduleState()
    data class Failed(val error: Throwable) : AddScheduleState()
}