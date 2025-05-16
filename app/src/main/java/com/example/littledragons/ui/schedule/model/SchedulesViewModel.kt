package com.example.littledragons.ui.schedule.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.SchedulesRepository
import com.example.littledragons.model.types.Schedule
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ISchedulesViewModel {
    val state: StateFlow<SchedulesState>
    val deleteResult: StateFlow<DeleteScheduleResult>
    fun load(
        user: SchedulesState.UserRole,
        timeRange: Pair<Timestamp, Timestamp>
    )

    fun delete(schedule: Schedule)
}

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    private val schedulesRepo: SchedulesRepository,
) : ViewModel(), ISchedulesViewModel {
    val _state = MutableStateFlow<SchedulesState>(SchedulesState.Initial)
    override val state: StateFlow<SchedulesState> = _state

    val _deleteResult = MutableStateFlow<DeleteScheduleResult>(DeleteScheduleResult.Initial)
    override val deleteResult: StateFlow<DeleteScheduleResult> = _deleteResult

    override fun load(
        user: SchedulesState.UserRole,
        timeRange: Pair<Timestamp, Timestamp>
    ) {
        if (state.value is SchedulesState.Loading) {
            return
        }

        viewModelScope.launch {
            when (val u = user) {
                is SchedulesState.UserRole.Parent if u.child?.classId == null -> {
                    _state.emit(SchedulesState.Failed.ChildNotFound)
                    return@launch
                }

                else -> {}
            }
            _state.emit(SchedulesState.Loading(currentUser = user, timeRange = timeRange))
            try {
                val list = when (val u = user) {
                    is SchedulesState.UserRole.Parent ->
                        schedulesRepo.get(
                            schoolClassId = u.child!!.classId!!,
                            timeRange = timeRange
                        )

                    is SchedulesState.UserRole.Teacher ->
                        schedulesRepo.getByTeacher(
                            teacherId = u.teacherId,
                            schoolClassId = u.schoolClassId,
                            timeRange = timeRange,
                        )
                }
                _state.emit(
                    SchedulesState.Loaded(
                        currentUser = user,
                        schedules = list,
                        timeRange = timeRange
                    )
                );
            } catch (e: Throwable) {
                Log.e("SchedulesViewModel", "Unable to fetch schedules list", e)
                _state.emit(
                    SchedulesState.Failed.Error(
                        currentUser = user,
                        timeRange = timeRange,
                        error = e
                    )
                )
            }
        }
    }

    override fun delete(schedule: Schedule) {
        viewModelScope.launch {
            _deleteResult.emit(DeleteScheduleResult.InProgress)

            try {
                removeFromList(schedule)
                schedulesRepo.delete(schedule)
                _deleteResult.emit(DeleteScheduleResult.Success)
            } catch (e: Throwable) {
                Log.e("SchedulesViewModel", "Unable to delete schedule", e)
                returnToList(schedule)
                _deleteResult.emit(DeleteScheduleResult.Failed(e))
            }
        }
    }

    private suspend fun removeFromList(schedule: Schedule) {
        when (val s = _state.value) {
            is SchedulesState.Loaded -> _state.emit(
                s.copy(schedules = s.schedules.filter { schedule != it })
            )

            else -> {}
        }
    }

    private suspend fun returnToList(schedule: Schedule) {
        when (val s = _state.value) {
            is SchedulesState.Loaded -> _state.emit(
                s.copy(schedules = (s.schedules + schedule).sortedByDescending { it.startTime })
            )

            else -> {}
        }
    }
}