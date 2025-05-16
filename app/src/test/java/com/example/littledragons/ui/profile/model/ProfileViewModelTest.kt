package com.example.littledragons.ui.profile.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.service.UserRepository
import com.example.littledragons.model.types.Student
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
class ProfileViewModelTest {
    private lateinit var authService: AuthService
    private lateinit var userRepo: UserRepository
    private lateinit var studentRepo: StudentsRepository
    private lateinit var viewModel: ProfileViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        authService = mockk()
        userRepo = mockk()
        studentRepo = mockk()
        viewModel = ProfileViewModel(userRepo, studentRepo, authService)
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
            childId = "id",
        )
        val child = Student(
            id = user.childId,
            firstName = "firstName",
            lastName = "lastName",
            classId = "classId"
        )

        viewModel.load(user = user, child = child)

        Assert.assertEquals(email, viewModel.email)
        Assert.assertEquals("$firstName $lastName", viewModel.name)
        Assert.assertEquals("${child.firstName} ${child.lastName}", viewModel.childName)
        Assert.assertEquals(child.classId, viewModel.childSchoolClass)
    }

    @Test
    fun submit() = runTest {
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
            childId = "id",
        )
        val child = Student(
            id = user.childId,
            firstName = "firstName",
            lastName = "lastName",
            classId = "classId"
        )

        val expectedStates = listOf(
            ChangeProfileState.Initial,
            ChangeProfileState.InProgress,
            ChangeProfileState.Success
        )
        val actualStates = mutableListOf<ChangeProfileState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.editState.toList(actualStates)
        }

        coEvery { studentRepo.find("first", "last", schoolClassId = "5А") } returns listOf(child)
        coEvery { authService.updateEmail("email@email.com") } just runs
        coEvery {
            userRepo.update(
                user.copy(
                    email = "email@email.com",
                    firstName = "first",
                    lastName = "last"
                )
            )
        } just runs

        viewModel.load(user = user, child = child)

        viewModel.updateEmail("email@email.com")
        viewModel.updateName("first last")
        viewModel.updateChildName("first last")
        viewModel.updateChildSchoolClass("5А")

        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.nameIsNotValid)
        Assert.assertFalse(viewModel.emailIsNotValid)
        Assert.assertFalse(viewModel.childNameIsNotValid)
        Assert.assertFalse(viewModel.childSchoolClassIsNotValid)

        coVerify { studentRepo.find("first", "last", schoolClassId = "5А") }
        coVerify { authService.updateEmail("email@email.com") }
        coVerify {
            userRepo.update(
                user.copy(
                    email = "email@email.com",
                    firstName = "first",
                    lastName = "last"
                )
            )
        }

        stateJob.cancel()
    }
}