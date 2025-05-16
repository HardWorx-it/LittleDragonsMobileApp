package com.example.littledragons.ui.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.service.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IAppViewModel {
    val state: StateFlow<AppState>
    val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult>
    val isEmailVerified: StateFlow<VerifyEmailState>
    fun load(force: Boolean = false)
    fun resendEmailVerify()
}

@HiltViewModel()
class AppViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val studentRepo: StudentsRepository,
    private val authService: AuthService
) : ViewModel(), IAppViewModel {
    private val _state = MutableStateFlow<AppState>(AppState.Initial)
    override val state: StateFlow<AppState> = _state

    val _resendEmailVerifyResult =
        MutableStateFlow<ResendEmailVerifyResult>(ResendEmailVerifyResult.Initial)
    override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
        _resendEmailVerifyResult

    fun init() {
        authService.removeAuthStateListener(this::onAuthStateChanged)
        authService.addAuthStateListener(this::onAuthStateChanged)
    }

    override fun onCleared() {
        authService.removeAuthStateListener(this::onAuthStateChanged)

        super.onCleared()
    }

    private fun onAuthStateChanged() {
        when (_state.value) {
            is AppState.Loaded if authService.isAuthorized() -> load(force = true)
            else -> load()
        }
    }

    override val isEmailVerified: StateFlow<VerifyEmailState> = flow {
        while (true) {
            try {
                authService.reload()
            } catch (e: Throwable) {
                Log.e("AppViewModel", "Unable to reload user", e)
            }
            if (authService.isVerified()) {
                emit(VerifyEmailState.Verified)
                break
            } else {
                emit(VerifyEmailState.NotVerified)
                delay(3000)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(6000),
        initialValue = VerifyEmailState.Initial,
    )

    override fun load(force: Boolean) {
        if (!force && (state.value == AppState.Loading || state.value is AppState.Loaded)) {
            return
        }
        viewModelScope.launch {
            _state.emit(AppState.Loading)

            val uid = authService.getUserUid()
            if (uid == null) {
                _state.emit(AppState.Failed.NotAuthorized)
            } else {
                try {
                    val user = userRepo.getByUid(uid)
                    if (user == null) {
                        _state.emit(AppState.Failed.NotRegistered)
                    } else {
                        val child = try {
                            user.childId?.let { studentRepo.getById(it) }
                        } catch (e: Throwable) {
                            Log.e("AppViewModel", "Unable to load child profile", e)
                            null
                        }
                        _state.emit(AppState.Loaded(user = user, child = child))
                    }
                } catch (e: Throwable) {
                    Log.e("AppViewModel", "Unable to load user profile", e)
                    _state.emit(AppState.Failed.Error(e))
                }
            }
        }
    }

    override fun resendEmailVerify() {
        viewModelScope.launch {
            try {
                authService.sendEmailVerification()
                _resendEmailVerifyResult.emit(ResendEmailVerifyResult.Success)
            } catch (e: Throwable) {
                Log.e("AppViewModel", "Unable to send e-mail verification", e)
                _resendEmailVerifyResult.emit(ResendEmailVerifyResult.Failed)
            }
        }
    }
}