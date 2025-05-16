package com.example.littledragons.ui.grades.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.GradesRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.Student
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
import kotlinx.datetime.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddGradeViewModelTest {
    private lateinit var gradesRepo: GradesRepository
    private lateinit var authService: AuthService
    private lateinit var viewModel: AddGradeViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        gradesRepo = mockk()
        authService = mockk()
        viewModel = AddGradeViewModel(gradesRepo, authService)
    }

    @Test
    fun add() = runTest {
        val expectedStates = listOf(
            AddGradeState.Initial,
            AddGradeState.InProgress,
            AddGradeState.Success
        )
        val actualStates = mutableListOf<AddGradeState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val grade = Grade(
            teacherId = "id",
            classId = "5А",
            studentId = "id",
            subjectId = "id",
            gradeValue = 5,
            date = LocalDate.fromEpochDays(0).toTimestamp()
        )

        coEvery { gradesRepo.add(grade) } just runs
        coEvery { authService.getUserUid() } returns "id"

        viewModel.setSchoolClassId("5А")
        viewModel.updateGrade(grade.gradeValue!!)
        viewModel.updateStudent(Student(id = "id"))
        viewModel.updateSubjectId("id")
        viewModel.updateDate(LocalDate.fromEpochDays(0))

        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.gradeIsNotValid)
        Assert.assertFalse(viewModel.studentIsNotValid)
        Assert.assertFalse(viewModel.subjectIsNotValid)

        coVerify { gradesRepo.add(grade) }
        coVerify { authService.getUserUid() }

        stateJob.cancel()
    }

    @Test
    fun update() = runTest {
        val expectedStates = listOf(
            AddGradeState.Initial,
            AddGradeState.InProgress,
            AddGradeState.Success
        )
        val actualStates = mutableListOf<AddGradeState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val grade = Grade(
            id = "id",
            teacherId = "id",
            classId = "5А",
            studentId = "id",
            subjectId = "id",
            gradeValue = 5,
            date = LocalDate.fromEpochDays(0).toTimestamp()
        )

        coEvery { gradesRepo.update(grade) } just runs
        coEvery { authService.getUserUid() } returns "id"

        viewModel.setExistsId(grade.id)
        viewModel.setSchoolClassId("5А")
        viewModel.updateGrade(grade.gradeValue!!)
        viewModel.updateStudent(Student(id = "id"))
        viewModel.updateSubjectId("id")
        viewModel.updateDate(LocalDate.fromEpochDays(0))

        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.gradeIsNotValid)
        Assert.assertFalse(viewModel.studentIsNotValid)
        Assert.assertFalse(viewModel.subjectIsNotValid)

        coVerify { gradesRepo.update(grade) }
        coVerify { authService.getUserUid() }

        stateJob.cancel()
    }
}