package com.example.littledragons.ui.auth.model

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.isEmail
import com.example.littledragons.model.isValidPassword
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.splitFirstAndLastName
import com.example.littledragons.model.types.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IRegisterViewModel {
    val name: String
    val email: String
    val password: String
    val passwordRepeat: String
    val userRole: UserRole
    val nameIsNotValid: Boolean
    val emailIsNotValid: Boolean
    val passwordIsNotValid: Boolean
    val passwordRepeatIsNotValid: Boolean
    val state: StateFlow<RegisterState>
    fun updateName(value: String)
    fun updateEmail(value: String)
    fun updatePassword(value: String)
    fun updatePasswordRepeat(value: String)
    fun updateUserRole(value: UserRole)
    fun submit()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel(), IRegisterViewModel {
    override var name by mutableStateOf("")
        private set
    override var email by mutableStateOf("")
        private set
    override var password by mutableStateOf("")
        private set
    override var passwordRepeat by mutableStateOf("")
        private set
    override var userRole by mutableStateOf(UserRole.Parent)
        private set

    override var nameIsNotValid by mutableStateOf(false)
        private set
    override var emailIsNotValid by mutableStateOf(false)
        private set
    override var passwordIsNotValid by mutableStateOf(false)
        private set
    override var passwordRepeatIsNotValid by mutableStateOf(false)
        private set


    private val formIsNotValid by derivedStateOf {
        nameIsNotValid || emailIsNotValid || passwordIsNotValid
    }

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Initial)
    override val state: StateFlow<RegisterState> = _state

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun updateEmail(value: String) {
        email = value
        emailIsNotValid = false
    }

    override fun updatePassword(value: String) {
        password = value
        passwordIsNotValid = false
    }

    override fun updatePasswordRepeat(value: String) {
        passwordRepeat = value
        passwordRepeatIsNotValid = false
    }

    override fun updateUserRole(value: UserRole) {
        userRole = value
    }

    // Отправка формы
    override fun submit() {
        name = name.trim()
        email = email.trim()
        password = password.trim()
        passwordRepeat = passwordRepeat.trim()

        nameIsNotValid = !nameIsValid()
        emailIsNotValid = !emailIsValid()
        passwordIsNotValid = !passwordIsValid()
        passwordRepeatIsNotValid = password != passwordRepeat

        if (formIsNotValid) {
            return
        }

        val (firstName, lastName) = splitFirstAndLastName(name)!!
        viewModelScope.launch {
            try {
                _state.emit(RegisterState.InProgress)
                authService.createUserWithEmailAndPassword(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    role = userRole,
                )

                try {
                    authService.sendEmailVerification()
                    _state.emit(RegisterState.Success)
                } catch (e: Throwable) {
                    Log.e("RegisterViewModel", "Unable to send e-mail verification", e)
                    _state.emit(RegisterState.Failed.EmailVerification)
                }
            } catch (e: Throwable) {
                Log.e("RegisterViewModel", "Unable to register user", e)
                _state.emit(RegisterState.Failed.Error(e))
            }
        }
    }

    private fun nameIsValid() = name.isNotEmpty() && splitFirstAndLastName(name) != null

    private fun emailIsValid() =
        // Проверка e-mail на корректный формат
        email.isNotEmpty() && isEmail(email)

    private fun passwordIsValid() =
        // Проверка что длина пароля на меньше 3 и не больше 30
        password.isNotEmpty() && isValidPassword(password)
}