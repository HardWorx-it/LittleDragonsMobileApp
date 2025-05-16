package com.example.littledragons.ui.subjects.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.SchoolSubjectsRepository
import com.example.littledragons.model.types.SchoolSubject
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
class SchoolSubjectsViewModelTest {
    private lateinit var subjectsRepo: SchoolSubjectsRepository
    private lateinit var viewModel: SchoolSubjectsViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun state() = runTest {
        subjectsRepo = mockk()
        coEvery { subjectsRepo.listenAll() } returns flowOf(listOf(SchoolSubject("name")))
        viewModel = SchoolSubjectsViewModel(subjectsRepo)

        val expectedStates = listOf(
            SchoolSubjectsState.Loading,
            SchoolSubjectsState.Loaded(listOf(SchoolSubject("name")))
        )
        val actualStates = mutableListOf<SchoolSubjectsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { subjectsRepo.listenAll() }

        stateJob.cancel()
    }
}