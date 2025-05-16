package com.example.littledragons.ui.profile.model

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.isValidSchoolClassName
import com.example.littledragons.model.service.SchoolClassesRepository
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.splitFirstAndLastName
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IAddStudentViewModel {
    val name: String
    val schoolClassId: SchoolClassId
    val nameIsNotValid: Boolean
    val schoolClassIdIsNotValid: Boolean
    val state: StateFlow<AddStudentState>
    fun updateName(value: String)
    fun updateSchoolClassId(value: String)
    fun submit()
}

@HiltViewModel
class AddStudentViewModel @Inject constructor(
    private val studentRepo: StudentsRepository,
    private val schoolClassRepo: SchoolClassesRepository
) : ViewModel(), IAddStudentViewModel {
    override var name by mutableStateOf("")
        private set
    override var schoolClassId by mutableStateOf("")
        private set
    override var nameIsNotValid by mutableStateOf(false)
        private set
    override var schoolClassIdIsNotValid by mutableStateOf(false)
        private set

    private val _state = MutableStateFlow<AddStudentState>(AddStudentState.Initial)
    override val state: StateFlow<AddStudentState> = _state

    val isFormNotValid by derivedStateOf {
        nameIsNotValid || schoolClassIdIsNotValid
    }

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun updateSchoolClassId(value: String) {
        schoolClassId = value
        schoolClassIdIsNotValid = false
    }

    override fun submit() {
        if (state == AddStudentState.InProgress) {
            return
        }

        name = name.trim()
        schoolClassId = schoolClassId.trim()
        nameIsNotValid = !nameIsValid()
        schoolClassIdIsNotValid = !isValidSchoolClassName(schoolClassId)

        if (isFormNotValid) {
            return
        }

        val (firstName, lastName) = splitFirstAndLastName(name)!!

        viewModelScope.launch {
            _state.emit(AddStudentState.InProgress)
            try {
                val schoolClass = schoolClassRepo.getById(schoolClassId)
                if (schoolClass != null) {
                    studentRepo.add(
                        Student(
                            firstName = firstName,
                            lastName = lastName,
                            classId = schoolClassId,
                        )
                    )
                    _state.emit(AddStudentState.Success)
                } else {
                    _state.emit(AddStudentState.Failed.SchoolClassNotFound)
                }
            } catch (e: Throwable) {
                Log.e("AddStudentViewModel", "Unable to save student", e)
                _state.emit(AddStudentState.Failed.Error(e))
            }
        }
    }

    private fun nameIsValid() = name.isNotEmpty() && splitFirstAndLastName(name) != null
}