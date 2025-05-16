package com.example.littledragons.ui.grades.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.GradesRepository
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.StudentId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IGradesViewModel {
    val state: StateFlow<GradesState>
    val deleteResult: StateFlow<DeleteGradeResult>
    fun load(user: GradesState.UserRole, subjectId: SchoolSubjectId)
    fun delete(schedule: GradeItem)
}

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val gradesRepo: GradesRepository,
    studentsRepo: StudentsRepository
) : ViewModel(), IGradesViewModel {
    private var studentsFlow: SharedFlow<Map<StudentId, Student>> = studentsRepo.listenAll()
        .map { list -> list.associateBy { it.id!! } }
        .shareIn(
            scope = viewModelScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(5000),
        )
    private var loadJob: Job? = null

    val _state = MutableStateFlow<GradesState>(GradesState.Initial)
    override val state: StateFlow<GradesState> = _state

    val _deleteResult = MutableStateFlow<DeleteGradeResult>(DeleteGradeResult.Initial)
    override val deleteResult: StateFlow<DeleteGradeResult> = _deleteResult

    override fun load(user: GradesState.UserRole, subjectId: SchoolSubjectId) {
        if (state.value is GradesState.Loading) {
            return
        }

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            when (val u = user) {
                is GradesState.UserRole.Parent if u.childId == null -> {
                    _state.emit(GradesState.Failed.ChildNotFound(currentSubject = subjectId))
                    return@launch
                }

                else -> {}
            }

            _state.emit(GradesState.Loading(currentUser = user, currentSubject = subjectId))

            studentsFlow.collect { students ->
                load(
                    user = user,
                    subjectId = subjectId,
                    students = students
                )
            }
        }
    }

    private suspend fun load(
        user: GradesState.UserRole,
        subjectId: SchoolSubjectId,
        students: Map<StudentId, Student>
    ) {
        try {
            val list = when (val u = user) {
                is GradesState.UserRole.Parent ->
                    gradesRepo.getByStudent(studentId = u.childId!!, subjectId = subjectId)

                is GradesState.UserRole.Teacher ->
                    gradesRepo.getByTeacher(
                        teacherId = u.teacherId,
                        schoolClassId = u.schoolClassId,
                        subjectId = subjectId
                    )
            }
            val items = list.mapNotNull { grade ->
                val student = students[grade.studentId]
                if (student == null) {
                    null
                } else {
                    GradeItem(student = student, grade = grade)
                }
            }
            _state.emit(
                GradesState.Loaded(
                    currentUser = user,
                    currentSubject = subjectId,
                    grades = items
                )
            )
        } catch (e: Throwable) {
            Log.e("GradesViewModel", "Unable to fetch grades list", e)
            _state.emit(
                GradesState.Failed.Error(currentUser = user, currentSubject = subjectId, error = e)
            )
        }
    }

    override fun delete(item: GradeItem) {
        viewModelScope.launch {
            _deleteResult.emit(DeleteGradeResult.InProgress)

            try {
                removeFromList(item)
                gradesRepo.delete(item.grade)
                _deleteResult.emit(DeleteGradeResult.Success)
            } catch (e: Throwable) {
                Log.e("GradesViewModel", "Unable to delete grade", e)
                returnToList(item)
                _deleteResult.emit(DeleteGradeResult.Failed(e))
            }
        }
    }

    private suspend fun removeFromList(item: GradeItem) {
        when (val s = _state.value) {
            is GradesState.Loaded -> _state.emit(
                s.copy(grades = s.grades.filter { item != it })
            )

            else -> {}
        }
    }

    private suspend fun returnToList(grade: GradeItem) {
        when (val s = _state.value) {
            is GradesState.Loaded -> _state.emit(
                s.copy(grades = (s.grades + grade).sortedBy { it.grade.date })
            )

            else -> {}
        }
    }
}