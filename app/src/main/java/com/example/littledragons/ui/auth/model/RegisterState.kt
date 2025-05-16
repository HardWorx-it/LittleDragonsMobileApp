package com.example.littledragons.ui.auth.model


sealed interface RegisterState {
    data object Initial : RegisterState
    data object InProgress : RegisterState
    data object Success : RegisterState
    sealed interface Failed : RegisterState {
        data object EmailVerification : Failed
        data class Error(val error: Throwable) : Failed
    }
}