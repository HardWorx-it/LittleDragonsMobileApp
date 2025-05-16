package com.example.littledragons.ui.events.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.EventsRepository
import com.example.littledragons.model.types.Event
import com.google.firebase.Timestamp
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
class EventsViewModelTest {
    private lateinit var eventRepo: EventsRepository
    private lateinit var viewModel: EventsViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        eventRepo = mockk()
        viewModel = EventsViewModel(eventRepo)
    }

    @Test
    fun load() = runTest {
        val expectedStates = listOf(
            EventsState.Initial,
            EventsState.Loading,
            EventsState.Loaded(listOf(Event(id = "id")))
        )
        val actualStates = mutableListOf<EventsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val dateRange = Timestamp(1, 0) to Timestamp(2, 0)

        coEvery { eventRepo.getAll(dateRange) } returns listOf(Event(id = "id"))

        viewModel.setDateRange(dateRange)
        viewModel.load()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { eventRepo.getAll(dateRange) }

        stateJob.cancel()
    }

    @Test
    fun delete() = runTest {
        val expectedStates = listOf(
            EventsState.Initial,
            EventsState.Loading,
            EventsState.Loaded(listOf(Event(id = "id"))),
            EventsState.Loaded(listOf())
        )
        val actualStates = mutableListOf<EventsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val dateRange = Timestamp(1, 0) to Timestamp(2, 0)

        coEvery { eventRepo.getAll(dateRange) } returns listOf(Event(id = "id"))
        coEvery { eventRepo.delete(Event(id = "id")) } just runs

        viewModel.setDateRange(dateRange)
        viewModel.load()
        viewModel.delete(Event(id = "id"))
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { eventRepo.getAll(dateRange) }
        coVerify { eventRepo.delete(Event(id = "id")) }

        stateJob.cancel()
    }
}