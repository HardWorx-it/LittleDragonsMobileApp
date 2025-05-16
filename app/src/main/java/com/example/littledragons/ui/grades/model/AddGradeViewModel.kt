package com.example.littledragons.ui.grades.model

import android.util.Log
import androidx.annotation.IntRange
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.GradesRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.EventId
import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.GradeId
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

interface IAddGradeViewModel {
    val subjectId: SchoolSubjectId
    val student: Student?
    val grade: Int?
    val gradeIsNotValid: Boolean
    val studentIsNotValid: Boolean
    val subjectIsNotValid: Boolean
    val date: LocalDate
    val refresh: SharedFlow<Boolean>
    val state: StateFlow<AddGradeState>
    fun setExistsId(value: GradeId?)
    fun setSchoolClassId(value: SchoolClassId)
    fun updateSubjectId(value: String)
    fun updateStudent(value: Student?)
    fun updateDate(value: LocalDate)
    fun updateGrade(@IntRange(from = 2, to = 5) value: Int)
    fun submit()
    fun clear()
}

@HiltViewModel
class AddGradeViewModel @Inject constructor(
    private val gradesRepo: GradesRepository,
    private val authService: AuthService,
) : ViewModel(), IAddGradeViewModel {
    private var existsId: EventId? = null
    private lateinit var schoolClassId: SchoolClassId
    override var subjectId: String by mutableStateOf("")
        private set
    override var student: Student? by mutableStateOf(null)
        private set
    override var grade: Int? by mutableStateOf(null)
        private set
    override var date: LocalDate by mutableStateOf(LocalDate.fromEpochDays(0))
        private set
    override var subjectIsNotValid: Boolean by mutableStateOf(false)
        private set
    override var gradeIsNotValid: Boolean by mutableStateOf(false)
        private set
    override var studentIsNotValid: Boolean by mutableStateOf(false)
        private set

    private val formIsNotValid by derivedStateOf {
        gradeIsNotValid || studentIsNotValid || subjectIsNotValid
    }

    val _state = MutableStateFlow<AddGradeState>(AddGradeState.Initial)
    override val state: StateFlow<AddGradeState> = _state

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

    override fun updateStudent(value: Student?) {
        student = value
        studentIsNotValid = false
    }

    override fun updateDate(value: LocalDate) {
        date = value
    }

    override fun updateGrade(value: Int) {
        grade = value
        gradeIsNotValid = false
    }

    override fun submit() {
        if (state == AddGradeState.InProgress) {
            return
        }

        schoolClassId = schoolClassId.trim()
        subjectId = subjectId.trim()

        subjectIsNotValid = !isSubjectValid()
        studentIsNotValid = !isStudentValid()
        gradeIsNotValid = !isGradeValid()

        if (formIsNotValid) {
            return
        }

        viewModelScope.launch {
            _state.emit(AddGradeState.InProgress)
            _refresh.emit(false)

            val grade = Grade(
                id = existsId,
                teacherId = authService.getUserUid(),
                subjectId = subjectId,
                classId = schoolClassId,
                studentId = student!!.id,
                gradeValue = grade,
                date = date.toTimestamp(),
            )

            try {
                if (grade.id != null) {
                    gradesRepo.update(grade)
                } else {
                    gradesRepo.add(grade)
                }
            } catch (e: Throwable) {
                Log.e("AddGradeViewModel", "Unable to add or change grade", e)
                _state.emit(AddGradeState.Failed(e))
            }

            clear()

            _state.emit(AddGradeState.Success)
            _refresh.emit(true)
        }
    }

    private fun isSubjectValid() = subjectId.isNotEmpty()

    private fun isStudentValid() = student != null

    private fun isGradeValid() = grade != null

    override fun clear() {
        subjectId = ""
        subjectIsNotValid = false
        date = LocalDate.fromEpochDays(0)
        student = null
        grade = null
        existsId = null
    }
}