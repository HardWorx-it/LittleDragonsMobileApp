package com.example.littledragons.ui.home.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.service.UserRepository
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.model.ResendEmailVerifyResult
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

class AppViewModelTest {
    private lateinit var authService: AuthService
    private lateinit var userRepo: UserRepository
    private lateinit var studentRepo: StudentsRepository
    private lateinit var viewModel: AppViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        authService = mockk()
        userRepo = mockk()
        studentRepo = mockk()
        viewModel = AppViewModel(
            userRepo,
            studentRepo,
            authService
        )
    }

    @Test
    fun isEmailVerified() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun load() = runTest {
        val uid = "1"
        val email = "test@example.org"
        val firstName = "First"
        val lastName = "Last"
        val role = UserRole.Parent
        val user = UserAccount(
            uid = uid,
            email = email,
            role = role,
            firstName = firstName,
            lastName = lastName,
            childId = "id"
        )
        val child = Student(
            id = user.childId,
            firstName = "firstName",
            lastName = "lastName",
            classId = "classId"
        )
        val expectedStates = listOf(
            AppState.Initial,
            AppState.Loading,
            AppState.Loaded(user = user, child = child),
        )
        val actualStates = mutableListOf<AppState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery { authService.getUserUid() } returns uid
        coEvery { userRepo.getByUid(uid) } returns user
        coEvery { studentRepo.getById(child.id!!) } returns child

        viewModel.load()
        advanceUntilIdle()

        Assert.assertEquals(
            expectedStates,
            actualStates
        )
        coVerify { authService.getUserUid() }
        coVerify { userRepo.getByUid(uid) }
        coVerify { studentRepo.getById(child.id!!) }
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `load (Not authorized)`() = runTest {
        val expectedStates = listOf(
            AppState.Initial,
            AppState.Loading,
            AppState.Failed.NotAuthorized,
        )
        val actualStates = mutableListOf<AppState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery { authService.getUserUid() } returns null

        viewModel.load()
        advanceUntilIdle()

        Assert.assertEquals(
            expectedStates,
            actualStates
        )
        coVerify { authService.getUserUid() }
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `load (Not registered)`() = runTest {
        val uid = "1"

        val expectedStates = listOf(
            AppState.Initial,
            AppState.Loading,
            AppState.Failed.NotRegistered,
        )
        val actualStates = mutableListOf<AppState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery { authService.getUserUid() } returns uid
        coEvery { userRepo.getByUid(uid) } returns null

        viewModel.load()
        advanceUntilIdle()

        Assert.assertEquals(
            expectedStates,
            actualStates
        )
        coVerify { authService.getUserUid() }
        coVerify { userRepo.getByUid(uid) }
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resendEmailVerify() = runTest {
        val expectedStates = listOf(
            ResendEmailVerifyResult.Initial,
            ResendEmailVerifyResult.Success,
        )
        val actualStates = mutableListOf<ResendEmailVerifyResult>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.resendEmailVerifyResult.toList(actualStates)
        }

        coEvery { authService.sendEmailVerification() } just runs

        viewModel.resendEmailVerify()
        advanceUntilIdle()

        Assert.assertEquals(
            expectedStates,
            actualStates
        )
        coVerify { authService.sendEmailVerification() }
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `resendEmailVerify (Failed)`() = runTest {
        val expectedStates = listOf(
            ResendEmailVerifyResult.Initial,
            ResendEmailVerifyResult.Failed,
        )
        val actualStates = mutableListOf<ResendEmailVerifyResult>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.resendEmailVerifyResult.toList(actualStates)
        }

        coEvery { authService.sendEmailVerification() } throws Exception()

        viewModel.resendEmailVerify()
        advanceUntilIdle()

        Assert.assertEquals(
            expectedStates,
            actualStates
        )
        coVerify { authService.sendEmailVerification() }
        stateJob.cancel()
    }
}