package com.example.littledragons.ui.schedule.model

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.SchedulesRepository
import com.example.littledragons.model.service.SchoolSubjectsRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.EventId
import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.ScheduleId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubject
import com.example.littledragons.model.types.SchoolSubjectId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import javax.inject.Inject

interface IAddScheduleViewModel {
    val subjectId: SchoolSubjectId
    val subjectIsNotValid: Boolean
    val timeRangeIsNotValid: Boolean
    val startTime: LocalDateTime
    val endTime: LocalDateTime
    val refresh: SharedFlow<Boolean>
    val state: StateFlow<AddScheduleState>
    fun setExistsId(value: ScheduleId?)
    fun setSchoolClassId(value: SchoolClassId)
    fun updateSubjectId(value: String)
    fun updateStartTime(value: LocalDateTime)
    fun updateEndTime(value: LocalDateTime)
    fun submit(createSubject: Boolean)
    fun clear()
}

@HiltViewModel
class AddScheduleViewModel @Inject constructor(
    private val scheduleRepo: SchedulesRepository,
    private val subjectsRepo: SchoolSubjectsRepository,
    private val authService: AuthService,
) : ViewModel(), IAddScheduleViewModel {
    private var existsId: EventId? = null
    private lateinit var schoolClassId: SchoolClassId
    override var subjectId: String by mutableStateOf("")
        private set
    override var startTime: LocalDateTime by mutableStateOf(
        LocalDateTime(
            LocalDate.fromEpochDays(0),
            LocalTime.fromMillisecondOfDay(0)
        )
    )
        private set
    override var endTime: LocalDateTime by mutableStateOf(
        LocalDateTime(
            LocalDate.fromEpochDays(0),
            LocalTime.fromMillisecondOfDay(0)
        )
    )
        private set
    override var subjectIsNotValid: Boolean by mutableStateOf(false)
        private set
    override var timeRangeIsNotValid: Boolean by mutableStateOf(false)
        private set

    private val formIsNotValid by derivedStateOf {
        subjectIsNotValid || timeRangeIsNotValid
    }

    val _state = MutableStateFlow<AddScheduleState>(AddScheduleState.Initial)
    override val state: StateFlow<AddScheduleState> = _state

    val _refresh = MutableSharedFlow<Boolean>()
    override val refresh: SharedFlow<Boolean> = _refresh

    override fun setExistsId(value: EventId?) {
        existsId = value
    }

    override fun setSchoolClassId(value: SchoolClassId) {
        schoolClassId = value
    }

    override fun updateSubjectId(value: String) {
        subjectId = value
        subjectIsNotValid = false
    }

    override fun updateStartTime(value: LocalDateTime) {
        startTime = value
        timeRangeIsNotValid = false
    }

    override fun updateEndTime(value: LocalDateTime) {
        endTime = value
        timeRangeIsNotValid = false
    }

    override fun submit(createSubject: Boolean) {
        if (state == AddScheduleState.InProgress) {
            return
        }

        schoolClassId = schoolClassId.trim()
        subjectId = subjectId.trim()

        subjectIsNotValid = !isSubjectValid()
        timeRangeIsNotValid = !isTimeRangeValid()

        if (formIsNotValid) {
            return
        }

        viewModelScope.launch {

            _state.emit(AddScheduleState.InProgress)
            _refresh.emit(false)

            val schedule = Schedule(
                id = existsId,
                subjectId = subjectId,
                classId = schoolClassId,
                startTime = startTime.toTimestamp(),
                endTime = endTime.toTimestamp(),
                teacherId = authService.getUserUid(),
            )

            async {
                try {
                    subjectsRepo.add(SchoolSubject(name = subjectId))

                    if (schedule.id != null) {
                        scheduleRepo.update(schedule)
                    } else {
                        scheduleRepo.add(schedule)
                    }
                } catch (e: Throwable) {
                    Log.e("AddScheduleViewModel", "Unable to add or change event", e)
                    _state.emit(AddScheduleState.Failed(e))
                }
            }.await()

            clear()

            _state.emit(AddScheduleState.Success)
            _refresh.emit(true)
        }
    }

    private fun isSubjectValid() = subjectId.isNotEmpty()
    private fun isTimeRangeValid() = endTime >= startTime

    override fun clear() {
        subjectId = ""
        subjectIsNotValid = false
        startTime = LocalDateTime(
            LocalDate.fromEpochDays(0),
            LocalTime.fromMillisecondOfDay(0)
        )
        endTime = startTime
        existsId = null
    }
}