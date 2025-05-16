package com.example.littledragons.ui.profile.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.isValidSchoolClassName
import com.example.littledragons.model.service.SchoolClassesRepository
import com.example.littledragons.model.types.SchoolClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IAddSchoolClassViewModel {
    val name: String
    val nameIsNotValid: Boolean
    val state: StateFlow<AddSchoolClassState>
    fun updateName(value: String)
    fun submit()
}

@HiltViewModel
class AddSchoolClassViewModel @Inject constructor(
    private val schoolClassRepo: SchoolClassesRepository
) : ViewModel(), IAddSchoolClassViewModel {
    override var name by mutableStateOf("")
        private set
    override var nameIsNotValid by mutableStateOf(false)
        private set

    private val _state = MutableStateFlow<AddSchoolClassState>(AddSchoolClassState.Initial)
    override val state: StateFlow<AddSchoolClassState> = _state

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun submit() {
        if (state == AddSchoolClassState.InProgress) {
            return
        }

        name = name.trim()
        nameIsNotValid = !isValidSchoolClassName(name)

        if (nameIsNotValid) {
            return
        }

        viewModelScope.launch {
            _state.emit(AddSchoolClassState.InProgress)
            try {
                schoolClassRepo.add(SchoolClass(name = name))
                _state.emit(AddSchoolClassState.Success)
            } catch (e: Throwable) {
                Log.e("AddSchoolClassViewModel", "Unable to save school class", e)
                _state.emit(AddSchoolClassState.Failed(e))
            }
        }
    }
}