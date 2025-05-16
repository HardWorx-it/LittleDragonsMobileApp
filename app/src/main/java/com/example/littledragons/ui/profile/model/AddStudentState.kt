package com.example.littledragons.ui.profile.model

sealed interface AddStudentState {
    data object Initial : AddStudentState
    data object InProgress : AddStudentState
    data object Success : AddStudentState
    sealed interface Failed : AddStudentState {
        data class Error(val error: Throwable) : Failed
        data object SchoolClassNotFound : Failed
    }
}