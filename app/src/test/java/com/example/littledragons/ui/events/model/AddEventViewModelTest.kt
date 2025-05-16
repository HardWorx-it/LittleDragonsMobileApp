package com.example.littledragons.ui.events.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.EventsRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Event
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
class AddEventViewModelTest {
    private lateinit var eventRepo: EventsRepository
    private lateinit var viewModel: AddEventViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        eventRepo = mockk()
        viewModel = AddEventViewModel(eventRepo)
    }

    @Test
    fun add() = runTest {
        val expectedStates = listOf(
            AddEventState.Initial,
            AddEventState.InProgress,
            AddEventState.Success
        )
        val actualStates = mutableListOf<AddEventState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val event = Event(
            title = "title",
            date = LocalDate.fromEpochDays(0).toTimestamp()
        )

        coEvery { eventRepo.add(event) } just runs

        viewModel.updateName(event.title!!)
        viewModel.updateDate(LocalDate.fromEpochDays(0))

        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.nameIsNotValid)

        coVerify { eventRepo.add(event) }

        stateJob.cancel()
    }

    @Test
    fun update() = runTest {
        val expectedStates = listOf(
            AddEventState.Initial,
            AddEventState.InProgress,
            AddEventState.Success
        )
        val actualStates = mutableListOf<AddEventState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val event = Event(
            id = "id",
            title = "title",
            date = LocalDate.fromEpochDays(0).toTimestamp()
        )

        coEvery { eventRepo.update(event) } just runs

        viewModel.setExistsId(event.id)
        viewModel.updateName(event.title!!)
        viewModel.updateDate(LocalDate.fromEpochDays(0))

        viewModel.submit()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.nameIsNotValid)

        coVerify { eventRepo.update(event) }

        stateJob.cancel()
    }
}