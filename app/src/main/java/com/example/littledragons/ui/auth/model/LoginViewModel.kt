package com.example.littledragons.ui.auth.model

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.isEmail
import com.example.littledragons.model.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILoginViewModel {
    val email: String
    val password: String
    val emailIsNotValid: Boolean
    val passwordIsNotValid: Boolean
    val state: StateFlow<LoginState>
    fun updateEmail(value: String)
    fun updatePassword(value: String)
    fun submit()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel(), ILoginViewModel {
    override var email by mutableStateOf("")
        private set
    override var password by mutableStateOf("")
        private set

    override var emailIsNotValid by mutableStateOf(false)
        private set
    override var passwordIsNotValid by mutableStateOf(false)
        private set

    private val formIsNotValid by derivedStateOf {
        emailIsNotValid || passwordIsNotValid
    }

    private val _state = MutableStateFlow<LoginState>(LoginState.Initial)
    override val state: StateFlow<LoginState> = _state

    override fun updateEmail(value: String) {
        email = value
        emailIsNotValid = false
    }

    override fun updatePassword(value: String) {
        password = value
        passwordIsNotValid = false
    }

    // Отправка формы
    override fun submit() {
        if (state.value == LoginState.InProgress || state.value is LoginState.Success) {
            return
        }

        email = email.trim()
        password = password.trim()

        emailIsNotValid = !emailIsValid()
        passwordIsNotValid = !passwordIsValid()


        if (formIsNotValid) {
            return
        }

        viewModelScope.launch {
            try {
                _state.emit(LoginState.InProgress)
                authService.signInWithEmailAndPassword(
                    email = email,
                    password = password,
                )
                _state.emit(LoginState.Success)
            } catch (e: Throwable) {
                Log.e("RegisterViewModel", "Unable to login user", e)
                _state.emit(LoginState.Failed(e))
            }
        }
    }

    private fun emailIsValid() =
        // Проверка e-mail на корректный формат
        email.isNotEmpty() && isEmail(email)

    private fun passwordIsValid() =
        // Проверка что пароль не пустой
        password.isNotEmpty()
}