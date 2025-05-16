package com.example.littledragons.ui.auth.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
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
class LoginViewModelTest {
    private lateinit var authService: AuthService
    private lateinit var viewModel: LoginViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        authService = mockk()
        viewModel = LoginViewModel(authService)
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
    fun submit() = runTest {
        val email = "test@example.org"
        val password = "Abcdef1+"
        coEvery { authService.signInWithEmailAndPassword(email, password) } just runs

        val expectedStates = listOf(
            LoginState.Initial,
            LoginState.InProgress,
            LoginState.Success,
        )
        val actualStates = mutableListOf<LoginState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        viewModel.updatePassword(password)
        viewModel.updateEmail(email)
        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { authService.signInWithEmailAndPassword(email, password) }
        stateJob.cancel()
    }

    @Test
    fun `submit (Failed)`() = runTest {
        val email = "test@example.org"
        val password = "Abcdef1+"
        val e = Exception()
        coEvery { authService.signInWithEmailAndPassword(email, password) } throws e

        val expectedStates = listOf(
            LoginState.Initial,
            LoginState.InProgress,
            LoginState.Failed(e),
        )
        val actualStates = mutableListOf<LoginState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        viewModel.updatePassword(password)
        viewModel.updateEmail(email)
        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { authService.signInWithEmailAndPassword(email, password) }
        stateJob.cancel()
    }

    @Test
    fun `submit (Invalid email and password)`() {
        val email = "test"
        val password = ""

        viewModel.updatePassword(password)
        viewModel.updateEmail(email)
        viewModel.submit()

        Assert.assertTrue(viewModel.emailIsNotValid)
        Assert.assertTrue(viewModel.passwordIsNotValid)
    }
}