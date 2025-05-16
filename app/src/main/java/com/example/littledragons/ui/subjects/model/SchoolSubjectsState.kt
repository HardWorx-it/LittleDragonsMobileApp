package com.example.littledragons.ui.subjects.model

import com.example.littledragons.model.types.SchoolSubject

sealed interface SchoolSubjectsState {
    data object Initial : SchoolSubjectsState
    data object Loading : SchoolSubjectsState
    data class Loaded(val subjects: List<SchoolSubject>) : SchoolSubjectsState
    data class Failed(val error: Throwable) : SchoolSubjectsState
}
