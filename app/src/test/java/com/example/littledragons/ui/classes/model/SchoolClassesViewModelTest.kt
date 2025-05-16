package com.example.littledragons.ui.classes.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.SchoolClassesRepository
import com.example.littledragons.model.types.SchoolClass
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
class SchoolClassesViewModelTest {
    private lateinit var classesRepo: SchoolClassesRepository
    private lateinit var viewModel: SchoolClassesViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun state() = runTest {
        classesRepo = mockk()
        coEvery { classesRepo.listenAll() } returns flowOf(listOf(SchoolClass("name")))
        viewModel = SchoolClassesViewModel(classesRepo)

        val expectedStates = listOf(
            SchoolClassesState.Loading,
            SchoolClassesState.Loaded(listOf(SchoolClass("name")))
        )
        val actualStates = mutableListOf<SchoolClassesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { classesRepo.listenAll() }

        stateJob.cancel()
    }
}