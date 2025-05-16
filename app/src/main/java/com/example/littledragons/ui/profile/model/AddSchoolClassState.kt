package com.example.littledragons.ui.profile.model

sealed interface AddSchoolClassState {
    data object Initial : AddSchoolClassState
    data object InProgress : AddSchoolClassState
    data object Success : AddSchoolClassState
    data class Failed(val error: Throwable) : AddSchoolClassState
}