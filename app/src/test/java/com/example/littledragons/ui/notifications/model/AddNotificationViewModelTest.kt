package com.example.littledragons.ui.notifications.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.NotificationsRepository
import com.example.littledragons.model.types.Notification
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
class AddNotificationViewModelTest {
    private lateinit var notificationsRepo: NotificationsRepository
    private lateinit var viewModel: AddNotificationViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        notificationsRepo = mockk()
        viewModel = AddNotificationViewModel(notificationsRepo)
    }

    @Test
    fun add() = runTest {
        val expectedStates = listOf(
            AddNotificationState.Initial,
            AddNotificationState.InProgress,
            AddNotificationState.Success
        )
        val actualStates = mutableListOf<AddNotificationState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val notification = Notification(
            title = "title",
            timestamp = Timestamp(1, 0)
        )

        coEvery { notificationsRepo.add(notification) } just runs

        viewModel.updateName(notification.title!!)

        viewModel.submit(timestamp = Timestamp(1, 0))
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.nameIsNotValid)

        coVerify { notificationsRepo.add(notification) }

        stateJob.cancel()
    }
}