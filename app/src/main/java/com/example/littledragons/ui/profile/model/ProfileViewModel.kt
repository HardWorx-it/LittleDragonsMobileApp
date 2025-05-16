package com.example.littledragons.ui.profile.model

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.decodeBase64String
import com.example.littledragons.model.encodeBase64Bytes
import com.example.littledragons.model.isEmail
import com.example.littledragons.model.isValidSchoolClassName
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.service.UserRepository
import com.example.littledragons.model.splitFirstAndLastName
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IProfileViewModel {
    val name: String
    val email: String
    val avatar: ByteArray?
    val childName: String
    val childSchoolClass: String
    val nameIsNotValid: Boolean
    val emailIsNotValid: Boolean
    val childNameIsNotValid: Boolean
    val childSchoolClassIsNotValid: Boolean
    val editState: StateFlow<ChangeProfileState>
    fun updateEmail(value: String)
    fun updateAvatar(value: ByteArray)
    fun updateName(value: String)
    fun updateChildName(value: String)
    fun updateChildSchoolClass(value: String)
    fun load(user: UserAccount, child: Student?)
    fun submit()
    fun logOut()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val studentRepo: StudentsRepository,
    private val authService: AuthService
) : ViewModel(), IProfileViewModel {
    private lateinit var user: UserAccount
    override var name by mutableStateOf("")
        private set
    override var email by mutableStateOf("")
        private set
    override var avatar by mutableStateOf<ByteArray?>(null)
        private set
    override var childName by mutableStateOf("")
        private set
    override var childSchoolClass by mutableStateOf("")

    override var nameIsNotValid by mutableStateOf(false)
        private set
    override var emailIsNotValid by mutableStateOf(false)
        private set
    override var childNameIsNotValid by mutableStateOf(false)
        private set
    override var childSchoolClassIsNotValid by mutableStateOf(false)
        private set

    private val formIsNotValid by derivedStateOf {
        nameIsNotValid || emailIsNotValid
    }

    private val parentFormIsNotValid by derivedStateOf {
        formIsNotValid || childNameIsNotValid || childSchoolClassIsNotValid
    }

    private val _editState =
        MutableStateFlow<ChangeProfileState>(ChangeProfileState.Initial)
    override val editState: StateFlow<ChangeProfileState> = _editState

    override fun updateEmail(value: String) {
        email = value
        emailIsNotValid = false
    }

    override fun updateAvatar(value: ByteArray) {
        avatar = value
    }

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun updateChildName(value: String) {
        childName = value
        childNameIsNotValid = false
    }

    override fun updateChildSchoolClass(value: String) {
        childSchoolClass = value
    }

    override fun load(user: UserAccount, child: Student?) {
        this.user = user
        email = user.email ?: ""
        name = "${user.firstName ?: ""} ${user.lastName ?: ""}"
        avatar = decodeBase64String(user.avatar)
        childName = child?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}" } ?: ""
        childSchoolClass = child?.classId ?: ""
    }

    override fun submit() {
        name = name.trim()
        email = email.trim()
        childName = childName.trim()
        childSchoolClass = childSchoolClass.trim()

        nameIsNotValid = !nameIsValid()
        emailIsNotValid = !emailIsValid()
        childNameIsNotValid = !childNameIsValid()
        childSchoolClassIsNotValid = !isValidSchoolClassName(childSchoolClass)

        val formIsNotValid = when (user.role) {
            UserRole.Parent -> parentFormIsNotValid
            UserRole.Teacher -> formIsNotValid
            null -> true
        }

        if (formIsNotValid) {
            return
        }

        viewModelScope.launch {
            try {
                _editState.emit(ChangeProfileState.InProgress)

                when (user.role) {
                    UserRole.Parent -> updateParentProfile(user)
                    UserRole.Teacher, null -> updateProfile(user)
                }

            } catch (_: FirebaseAuthRecentLoginRequiredException) {
                _editState.emit(ChangeProfileState.Failed.ReauthenticateRequired)
            } catch (e: Throwable) {
                Log.e(
                    "ProfileViewModel",
                    "Unable to update user profile",
                    e
                )
                _editState.emit(ChangeProfileState.Failed.Error(e))
            }
        }
    }

    private suspend fun updateParentProfile(user: UserAccount) {
        val (childFirstName, childLastName) = splitFirstAndLastName(childName)!!
        val students = studentRepo.find(
            firstName = childFirstName,
            lastName = childLastName,
            schoolClassId = childSchoolClass,
        )
        if (students?.isNotEmpty() == true) {
            updateProfile(
                user,
                child = students.first()
            )
        } else {
            _editState.emit(ChangeProfileState.Failed.ChildNotFound)
        }
    }

    private suspend fun updateProfile(
        user: UserAccount,
        child: Student? = null
    ) {
        val (firstName, lastName) = splitFirstAndLastName(name)!!

        if (email != user.email) {
            authService.updateEmail(email)
        }
        userRepo.update(
            user.copy(
                firstName = firstName,
                lastName = lastName,
                email = email,
                avatar = encodeBase64Bytes(avatar),
                childId = child?.id,
            )
        )
        _editState.emit(ChangeProfileState.Success)
    }

    override fun logOut() {
        authService.signOut()
    }

    private fun nameIsValid() = name.isNotEmpty() && splitFirstAndLastName(name) != null

    private fun emailIsValid() =
        // Проверка e-mail на корректный формат
        email.isNotEmpty() && isEmail(email)

    private fun childNameIsValid() =
        childName.isEmpty() || splitFirstAndLastName(name) != null
}