package com.example.littledragons.ui.model

import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount

sealed interface AppState {
    data object Initial : AppState
    data object Loading : AppState
    data class Loaded(val user: UserAccount, val child: Student?) : AppState
    sealed interface Failed : AppState {
        data object NotAuthorized : Failed
        data object NotRegistered : Failed
        data class Error(val error: Throwable) : Failed
    }
}