package com.example.littledragons.ui.auth.model


sealed interface LoginState {
    data object Initial : LoginState
    data object InProgress : LoginState
    data object Success : LoginState
    data class Failed(val error: Throwable) : LoginState
}