package com.example.littledragons.ui.notifications.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.NotificationsRepository
import com.example.littledragons.model.types.Notification
import com.example.littledragons.ui.notifications.model.model.NotificationsViewModel
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
class NotificationsViewModelTest {
    private lateinit var notificationsRepo: NotificationsRepository
    private lateinit var viewModel: NotificationsViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        notificationsRepo = mockk()
        viewModel = NotificationsViewModel(notificationsRepo)
    }

    @Test
    fun load() = runTest {
        val expectedStates = listOf(
            NotificationsState.Initial,
            NotificationsState.Loading,
            NotificationsState.Loaded(listOf(Notification(id = "id")))
        )
        val actualStates = mutableListOf<NotificationsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery { notificationsRepo.getAll() } returns listOf(Notification(id = "id"))

        viewModel.load()
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { notificationsRepo.getAll() }

        stateJob.cancel()
    }

    @Test
    fun delete() = runTest {
        val expectedStates = listOf(
            NotificationsState.Initial,
            NotificationsState.Loading,
            NotificationsState.Loaded(listOf(Notification(id = "id"))),
            NotificationsState.Loaded(listOf())
        )
        val actualStates = mutableListOf<NotificationsState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery { notificationsRepo.getAll() } returns listOf(Notification(id = "id"))
        coEvery { notificationsRepo.delete(Notification(id = "id")) } just runs

        viewModel.load()
        viewModel.delete(Notification(id = "id"))
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify { notificationsRepo.getAll() }
        coVerify { notificationsRepo.delete(Notification(id = "id")) }

        stateJob.cancel()
    }
}