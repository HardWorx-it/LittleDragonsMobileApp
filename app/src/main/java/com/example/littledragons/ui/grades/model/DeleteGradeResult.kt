package com.example.littledragons.ui.grades.model

sealed interface DeleteGradeResult {
    data object Initial : DeleteGradeResult
    data object InProgress : DeleteGradeResult
    data object Success : DeleteGradeResult
    data class Failed(val error: Throwable) : DeleteGradeResult
}