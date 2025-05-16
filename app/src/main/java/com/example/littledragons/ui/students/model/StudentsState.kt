package com.example.littledragons.ui.students.model

import com.example.littledragons.model.types.Student

sealed interface StudentsState {
    data object Initial : StudentsState
    data object Loading : StudentsState
    data class Loaded(val students: List<Student>) : StudentsState
    data class Failed(val error: Throwable) : StudentsState
}
