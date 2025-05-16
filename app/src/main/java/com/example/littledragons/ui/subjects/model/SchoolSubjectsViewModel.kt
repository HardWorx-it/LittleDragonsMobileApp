package com.example.littledragons.ui.subjects.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.SchoolSubjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface ISchoolSubjectsViewModel {
    val state: StateFlow<SchoolSubjectsState>
}

@HiltViewModel
class SchoolSubjectsViewModel @Inject constructor(
    subjectsRepo: SchoolSubjectsRepository,
) : ViewModel(), ISchoolSubjectsViewModel {
    override val state: StateFlow<SchoolSubjectsState> = subjectsRepo.listenAll()
        .map {
            SchoolSubjectsState.Loaded(
                subjects = it.sortedBy { it.name }
            )
        }
        .catch {
            Log.e("SchoolSubjectsViewModel", "Unable to fetch subjects list", it)
            SchoolSubjectsState.Failed(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchoolSubjectsState.Loading,
        )
}