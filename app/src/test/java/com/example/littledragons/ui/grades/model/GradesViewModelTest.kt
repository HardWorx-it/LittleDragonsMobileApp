package com.example.littledragons.ui.grades.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.GradesRepository
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.Student
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class GradesViewModelTest {
    private lateinit var gradesRepo: GradesRepository
    private lateinit var studentsRepo: StudentsRepository
    private lateinit var viewModel: GradesViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        gradesRepo = mockk()
        studentsRepo = mockk()

        coEvery { studentsRepo.listenAll() } returns flowOf(listOf(Student(id = "id")))

        viewModel = GradesViewModel(gradesRepo, studentsRepo)
    }


    @Test
    fun `Load by parent`() = runTest {
        val expectedStates = listOf(
            GradesState.Initial,
            GradesState.Loading(
                currentUser = GradesState.UserRole.Parent(childId = "id"),
                currentSubject = "id",
            ),
            GradesState.Loaded(
                currentUser = GradesState.UserRole.Parent(childId = "id"),
                currentSubject = "id",
                grades = listOf(
                    GradeItem(
                        student = Student(id = "id"),
                        grade = Grade(id = "id", studentId = "id")
                    )
                )
            )
        )
        val actualStates = mutableListOf<GradesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery {
            gradesRepo.getByStudent(
                studentId = "id",
                subjectId = "id"
            )
        } returns listOf(Grade(id = "id", studentId = "id"))

        viewModel.load(user = GradesState.UserRole.Parent(childId = "id"), subjectId = "id")
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            gradesRepo.getByStudent(
                studentId = "id",
                subjectId = "id"
            )
        }
        coVerify { studentsRepo.listenAll() }

        stateJob.cancel()
    }

    @Test
    fun `Load by teacher`() = runTest {
        val expectedStates = listOf(
            GradesState.Initial,
            GradesState.Loading(
                currentUser = GradesState.UserRole.Teacher(teacherId = "id", schoolClassId = "id"),
                currentSubject = "id",
            ),
            GradesState.Loaded(
                currentUser = GradesState.UserRole.Teacher(teacherId = "id", schoolClassId = "id"),
                currentSubject = "id",
                grades = listOf(
                    GradeItem(
                        student = Student(id = "id"),
                        grade = Grade(id = "id", studentId = "id")
                    )
                )
            )
        )
        val actualStates = mutableListOf<GradesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery {
            gradesRepo.getByTeacher(
                teacherId = "id",
                schoolClassId = "id",
                subjectId = "id"
            )
        } returns listOf(Grade(id = "id", studentId = "id"))

        viewModel.load(
            user = GradesState.UserRole.Teacher(teacherId = "id", schoolClassId = "id"),
            subjectId = "id"
        )
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            gradesRepo.getByTeacher(
                teacherId = "id",
                schoolClassId = "id",
                subjectId = "id"
            )
        }
        coVerify { studentsRepo.listenAll() }

        stateJob.cancel()
    }

    @Test
    fun delete() = runTest {
        coEvery {
            gradesRepo.getByStudent(
                studentId = "id",
                subjectId = "id"
            )
        } returns listOf(Grade(id = "id", studentId = "id"))
        coEvery { gradesRepo.delete(Grade(id = "id", studentId = "id")) } just runs

        viewModel.load(user = GradesState.UserRole.Parent(childId = "id"), subjectId = "id")
        viewModel.delete(
            GradeItem(
                student = Student(id = "id"),
                grade = Grade(id = "id", studentId = "id")
            )
        )
        advanceUntilIdle()

        coVerify {
            gradesRepo.getByStudent(
                studentId = "id",
                subjectId = "id"
            )
        }
        coVerify { gradesRepo.delete(Grade(id = "id", studentId = "id")) }
        coVerify { studentsRepo.listenAll() }
    }
}