package com.example.littledragons.ui.schedule.model

sealed interface DeleteScheduleResult {
    data object Initial : DeleteScheduleResult
    data object InProgress : DeleteScheduleResult
    data object Success : DeleteScheduleResult
    data class Failed(val error: Throwable) : DeleteScheduleResult
}