package com.example.littledragons.ui.schedule.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.SchedulesRepository
import com.example.littledragons.model.service.SchoolSubjectsRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.SchoolSubject
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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddScheduleViewModelTest {
    private lateinit var schedulesRepo: SchedulesRepository
    private lateinit var subjectsRepo: SchoolSubjectsRepository
    private lateinit var authService: AuthService
    private lateinit var viewModel: AddScheduleViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        schedulesRepo = mockk()
        subjectsRepo = mockk()
        authService = mockk()
        viewModel = AddScheduleViewModel(schedulesRepo, subjectsRepo, authService)
    }

    @Test
    fun add() = runTest {
        val expectedStates = listOf(
            AddScheduleState.Initial,
            AddScheduleState.InProgress,
            AddScheduleState.Success
        )
        val actualStates = mutableListOf<AddScheduleState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val schedule = Schedule(
            teacherId = "id",
            classId = "5А",
            subjectId = "id",
            startTime = LocalDate.fromEpochDays(1).toTimestamp(),
            endTime = LocalDate.fromEpochDays(2).toTimestamp()
        )

        coEvery { schedulesRepo.add(schedule) } just runs
        coEvery { authService.getUserUid() } returns "id"
        coEvery { subjectsRepo.add(SchoolSubject(name = "id")) } just runs

        viewModel.setSchoolClassId(schedule.classId!!)
        viewModel.updateSubjectId(schedule.subjectId!!)
        viewModel.updateStartTime(
            LocalDate.fromEpochDays(1).atTime(LocalTime.fromMillisecondOfDay(0))
        )
        viewModel.updateEndTime(
            LocalDate.fromEpochDays(2).atTime(LocalTime.fromMillisecondOfDay(0))
        )

        viewModel.submit(createSubject = true)
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.subjectIsNotValid)
        Assert.assertFalse(viewModel.timeRangeIsNotValid)

        coVerify { schedulesRepo.add(schedule) }
        coVerify { authService.getUserUid() }
        coVerify { subjectsRepo.add(SchoolSubject(name = "id")) }

        stateJob.cancel()
    }

    @Test
    fun update() = runTest {
        val expectedStates = listOf(
            AddScheduleState.Initial,
            AddScheduleState.InProgress,
            AddScheduleState.Success
        )
        val actualStates = mutableListOf<AddScheduleState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        val schedule = Schedule(
            id = "id",
            teacherId = "id",
            classId = "5А",
            subjectId = "id",
            startTime = LocalDate.fromEpochDays(1).toTimestamp(),
            endTime = LocalDate.fromEpochDays(2).toTimestamp()
        )

        coEvery { schedulesRepo.update(schedule) } just runs
        coEvery { authService.getUserUid() } returns "id"
        coEvery { subjectsRepo.add(SchoolSubject(name = "id")) } just runs

        viewModel.setExistsId(schedule.id)
        viewModel.setSchoolClassId(schedule.classId!!)
        viewModel.updateSubjectId(schedule.subjectId!!)
        viewModel.updateStartTime(
            LocalDate.fromEpochDays(1).atTime(LocalTime.fromMillisecondOfDay(0))
        )
        viewModel.updateEndTime(
            LocalDate.fromEpochDays(2).atTime(LocalTime.fromMillisecondOfDay(0))
        )

        viewModel.submit(createSubject = true)
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        Assert.assertFalse(viewModel.subjectIsNotValid)
        Assert.assertFalse(viewModel.timeRangeIsNotValid)

        coVerify { schedulesRepo.update(schedule) }
        coVerify { authService.getUserUid() }
        coVerify { subjectsRepo.add(SchoolSubject(name = "id")) }

        stateJob.cancel()
    }
}