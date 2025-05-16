package com.example.littledragons.ui.students.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.types.Student
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentsViewModelTest {
    private lateinit var studentsRepo: StudentsRepository
    private lateinit var viewModel: StudentsViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun state() = runTest {
        studentsRepo = mockk()
        coEvery { studentsRepo.listenAll() } returns flowOf(listOf(Student(id = "id")))
        viewModel = StudentsViewModel(studentsRepo)

        val expectedStates = listOf(
            StudentsState.Loading,
            StudentsState.Loaded(listOf(Student(id = "id")))
        )
        val actualStates = mutableListOf<StudentsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { studentsRepo.listenAll() }

        stateJob.cancel()
    }
}