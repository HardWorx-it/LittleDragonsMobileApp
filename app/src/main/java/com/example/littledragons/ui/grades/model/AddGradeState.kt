package com.example.littledragons.ui.grades.model

sealed class AddGradeState {
    data object Initial : AddGradeState()
    data object InProgress : AddGradeState()
    data object Success : AddGradeState()
    data class Failed(val error: Throwable) : AddGradeState()
}