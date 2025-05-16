package com.example.littledragons.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.routes.AuthDestination
import com.example.littledragons.ui.routes.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    val startDestination: StateFlow<Destination?> = flow<Destination> {
        emit(
            if (!authService.isAuthorized()) {
                AuthDestination.Login
            } else {
                AppDestination.Home
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )
}