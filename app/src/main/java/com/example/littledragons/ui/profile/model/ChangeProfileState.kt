package com.example.littledragons.ui.profile.model

sealed interface ChangeProfileState {
    data object Initial : ChangeProfileState
    data object InProgress : ChangeProfileState
    data object Success : ChangeProfileState
    sealed interface Failed : ChangeProfileState {
        data object ReauthenticateRequired : Failed
        data class Error(val error: Throwable) : Failed
        data object ChildNotFound : Failed
    }
}