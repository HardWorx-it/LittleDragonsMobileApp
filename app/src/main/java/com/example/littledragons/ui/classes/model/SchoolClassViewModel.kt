package com.example.littledragons.ui.classes.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.SchoolClassesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface ISchoolClassesViewModel {
    val state: StateFlow<SchoolClassesState>
}

@HiltViewModel
class SchoolClassesViewModel @Inject constructor(
    classesRepo: SchoolClassesRepository,
) : ViewModel(), ISchoolClassesViewModel {
    override val state: StateFlow<SchoolClassesState> = classesRepo.listenAll()
        .map {
            SchoolClassesState.Loaded(
                classes = it.sortedBy { it.name }
            )
        }
        .catch {
            Log.e("SchoolSubjectsViewModel", "Unable to fetch school classes list", it)
            SchoolClassesState.Failed(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchoolClassesState.Loading,
        )
}