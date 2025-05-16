package com.example.littledragons.ui.classes.model

import com.example.littledragons.model.types.SchoolClass

sealed interface SchoolClassesState {
    data object Initial : SchoolClassesState
    data object Loading : SchoolClassesState
    data class Loaded(val classes: List<SchoolClass>) : SchoolClassesState

    data class Failed(val error: Throwable) : SchoolClassesState
}
