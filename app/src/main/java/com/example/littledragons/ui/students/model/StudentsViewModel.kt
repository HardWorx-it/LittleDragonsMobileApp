package com.example.littledragons.ui.students.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface IStudentsViewModel {
    val state: StateFlow<StudentsState>
}

@HiltViewModel
class StudentsViewModel @Inject constructor(
    studentsRepo: StudentsRepository,
) : ViewModel(), IStudentsViewModel {
    override val state: StateFlow<StudentsState> = studentsRepo.listenAll()
        .map {
            StudentsState.Loaded(students = it)
        }
        .catch {
            Log.e("StudentsViewModel", "Unable to fetch students list", it)
            StudentsState.Failed(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudentsState.Loading,
        )
}