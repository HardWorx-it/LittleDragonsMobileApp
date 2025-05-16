package com.example.littledragons.ui.schedule.model

import com.example.littledragons.MainCoroutineRule
import com.example.littledragons.model.service.SchedulesRepository
import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.Student
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
class SchedulesViewModelTest {
    private lateinit var schedulesRepo: SchedulesRepository
    private lateinit var viewModel: SchedulesViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        schedulesRepo = mockk()
        viewModel = SchedulesViewModel(schedulesRepo)
    }


    @Test
    fun `Load by parent`() = runTest {
        val timeRange = Timestamp(1, 0) to Timestamp(2, 0)

        val expectedStates = listOf(
            SchedulesState.Initial,
            SchedulesState.Loading(
                currentUser = SchedulesState.UserRole.Parent(
                    child = Student(
                        id = "id",
                        classId = "id"
                    )
                ),
                timeRange = timeRange,
            ),
            SchedulesState.Loaded(
                currentUser = SchedulesState.UserRole.Parent(
                    child = Student(
                        id = "id",
                        classId = "id"
                    )
                ),
                timeRange = timeRange,
                schedules = listOf(Schedule(id = "id"))
            )
        )
        val actualStates = mutableListOf<SchedulesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery {
            schedulesRepo.get(
                schoolClassId = "id",
                timeRange = timeRange,
            )
        } returns listOf(Schedule(id = "id"))

        viewModel.load(
            user = SchedulesState.UserRole.Parent(child = Student(id = "id", classId = "id")),
            timeRange = timeRange
        )
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            schedulesRepo.get(
                schoolClassId = "id",
                timeRange = timeRange,
            )
        }

        stateJob.cancel()
    }

    @Test
    fun `Load by teacher`() = runTest {
        val timeRange = Timestamp(1, 0) to Timestamp(2, 0)

        val expectedStates = listOf(
            SchedulesState.Initial,
            SchedulesState.Loading(
                currentUser = SchedulesState.UserRole.Teacher(
                    teacherId = "id",
                    schoolClassId = "id"
                ),
                timeRange = timeRange,
            ),
            SchedulesState.Loaded(
                currentUser = SchedulesState.UserRole.Teacher(
                    teacherId = "id",
                    schoolClassId = "id"
                ),
                timeRange = timeRange,
                schedules = listOf(Schedule(id = "id"))
            )
        )
        val actualStates = mutableListOf<SchedulesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery {
            schedulesRepo.getByTeacher(
                teacherId = "id",
                schoolClassId = "id",
                timeRange = timeRange,
            )
        } returns listOf(Schedule(id = "id"))

        viewModel.load(
            user = SchedulesState.UserRole.Teacher(teacherId = "id", schoolClassId = "id"),
            timeRange = timeRange
        )
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            schedulesRepo.getByTeacher(
                teacherId = "id",
                schoolClassId = "id",
                timeRange = timeRange,
            )
        }

        stateJob.cancel()
    }

    @Test
    fun delete() = runTest {
        val timeRange = Timestamp(1, 0) to Timestamp(2, 0)

        val expectedStates = listOf(
            SchedulesState.Initial,
            SchedulesState.Loading(
                currentUser = SchedulesState.UserRole.Parent(
                    child = Student(
                        id = "id",
                        classId = "id"
                    )
                ),
                timeRange = timeRange,
            ),
            SchedulesState.Loaded(
                currentUser = SchedulesState.UserRole.Parent(
                    child = Student(
                        id = "id",
                        classId = "id"
                    )
                ),
                timeRange = timeRange,
                schedules = listOf(Schedule(id = "id"))
            ),
            SchedulesState.Loaded(
                currentUser = SchedulesState.UserRole.Parent(
                    child = Student(
                        id = "id",
                        classId = "id"
                    )
                ),
                timeRange = timeRange,
                schedules = listOf()
            )
        )
        val actualStates = mutableListOf<SchedulesState>()
        val stateJob = launch(UnconfinedTestDispatcher()) {
            viewModel.state.toList(actualStates)
        }

        coEvery {
            schedulesRepo.get(
                schoolClassId = "id",
                timeRange = timeRange,
            )
        } returns listOf(Schedule(id = "id"))
        coEvery { schedulesRepo.delete(Schedule(id = "id")) } just runs

        viewModel.load(
            user = SchedulesState.UserRole.Parent(child = Student(id = "id", classId = "id")),
            timeRange = timeRange
        )
        viewModel.delete(Schedule(id = "id"))
        advanceUntilIdle()

        Assert.assertEquals(expectedStates, actualStates)
        coVerify {
            schedulesRepo.get(
                schoolClassId = "id",
                timeRange = timeRange,
            )
        }
        coVerify { schedulesRepo.delete(Schedule(id = "id")) }

        stateJob.cancel()
    }
}