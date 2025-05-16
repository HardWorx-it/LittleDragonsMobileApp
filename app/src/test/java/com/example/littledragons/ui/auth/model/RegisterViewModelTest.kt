package com.example.littledragons.ui.auth.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private lateinit var authService: AuthService
    private lateinit var viewModel: RegisterViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        authService = mockk()
        viewModel = RegisterViewModel(authService)
    }

    @Test
    fun updateEmail() {
        viewModel.updateEmail("before")
        Assert.assertEquals("before", viewModel.email)
        viewModel.updateEmail("after")
        Assert.assertEquals("after", viewModel.email)
    }

    @Test
    fun updatePassword() {
        viewModel.updatePassword("before")
        Assert.assertEquals("before", viewModel.password)
        viewModel.updatePassword("after")
        Assert.assertEquals("after", viewModel.password)
    }

    @Test
    fun updatePasswordRepeat() {
        viewModel.updatePasswordRepeat("before")
        Assert.assertEquals("before", viewModel.passwordRepeat)
        viewModel.updatePasswordRepeat("after")
        Assert.assertEquals("after", viewModel.passwordRepeat)
    }

    @Test
    fun updateName() {
        viewModel.updateName("before")
        Assert.assertEquals("before", viewModel.name)
        viewModel.updateName("after")
        Assert.assertEquals("after", viewModel.name)
    }

    @Test
    fun updateUserRole() {
        viewModel.updateUserRole(UserRole.Parent)
        Assert.assertEquals(UserRole.Parent, viewModel.userRole)
        viewModel.updateUserRole(UserRole.Teacher)
        Assert.assertEquals(UserRole.Teacher, viewModel.userRole)
    }

    @Test
    fun submit() = runTest {
        val email = "test@example.org"
        val password = "Abcdef1+"
        val firstName = "First"
        val lastName = "Last"
        val name = "$firstName $lastName"
        val role = UserRole.Parent
        val user = UserAccount(
            uid = "1",
            email = email,
            role = role,
            firstName = firstName,
            lastName = lastName
        )

        coEvery {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        } returns user

        coEvery { authService.sendEmailVerification() } just runs

        val expectedStates = listOf(
            RegisterState.Initial,
            RegisterState.InProgress,
            RegisterState.Success,
        )
        val actualStates = mutableListOf<RegisterState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        viewModel.updatePassword(password)
        viewModel.updatePasswordRepeat(password)
        viewModel.updateEmail(email)
        viewModel.updateName(name)
        viewModel.updateUserRole(role)
        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        }
        coVerify { authService.sendEmailVerification() }
        stateJob.cancel()
    }

    @Test
    fun `submit (Failed)`() = runTest {
        val email = "test@example.org"
        val password = "Abcdef1+"
        val firstName = "First"
        val lastName = "Last"
        val name = "$firstName $lastName"
        val role = UserRole.Parent
        val user = UserAccount(
            uid = "1",
            email = email,
            role = role,
            firstName = firstName,
            lastName = lastName
        )
        val e = Exception()

        coEvery {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        } throws e

        val expectedStates = listOf(
            RegisterState.Initial,
            RegisterState.InProgress,
            RegisterState.Failed.Error(e),
        )
        val actualStates = mutableListOf<RegisterState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        viewModel.updatePassword(password)
        viewModel.updatePasswordRepeat(password)
        viewModel.updateEmail(email)
        viewModel.updateName(name)
        viewModel.updateUserRole(role)
        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        }
        stateJob.cancel()
    }

    @Test
    fun `submit (Invalid email, password, name)`() {
        val email = "test"
        val password = "123"
        val name = "name"

        viewModel.updatePassword(password)
        viewModel.updatePasswordRepeat("")
        viewModel.updateEmail(email)
        viewModel.updateName(name)
        viewModel.submit()

        Assert.assertTrue(viewModel.emailIsNotValid)
        Assert.assertTrue(viewModel.passwordIsNotValid)
        Assert.assertTrue(viewModel.nameIsNotValid)
        Assert.assertTrue(viewModel.passwordRepeatIsNotValid)
    }

    @Test
    fun `submit (E-mail verification failed)`() = runTest {
        val email = "test@example.org"
        val password = "Abcdef1+"
        val firstName = "First"
        val lastName = "Last"
        val name = "$firstName $lastName"
        val role = UserRole.Parent
        val user = UserAccount(
            uid = "1",
            email = email,
            role = role,
            firstName = firstName,
            lastName = lastName
        )
        val e = Exception()

        coEvery {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        } returns user

        coEvery { authService.sendEmailVerification() } throws e

        val expectedStates = listOf(
            RegisterState.Initial,
            RegisterState.InProgress,
            RegisterState.Failed.EmailVerification,
        )
        val actualStates = mutableListOf<RegisterState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        viewModel.updatePassword(password)
        viewModel.updatePasswordRepeat(password)
        viewModel.updateEmail(email)
        viewModel.updateName(name)
        viewModel.updateUserRole(role)
        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        }
        coVerify { authService.sendEmailVerification() }
        stateJob.cancel()
    }
}