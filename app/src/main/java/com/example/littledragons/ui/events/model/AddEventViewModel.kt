package com.example.littledragons.ui.events.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.EventsRepository
import com.example.littledragons.model.toTimestamp
import com.example.littledragons.model.types.Event
import com.example.littledragons.model.types.EventId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

interface IAddEventViewModel {
    val name: String
    val nameIsNotValid: Boolean
    val date: LocalDate
    val refresh: SharedFlow<Boolean>
    val state: StateFlow<AddEventState>
    fun setExistsId(value: EventId?)
    fun updateName(value: String)
    fun updateDate(value: LocalDate)
    fun submit()
    fun clear()
}

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val eventRepo: EventsRepository
) : ViewModel(), IAddEventViewModel {
    private var existsId: EventId? = null
    override var name: String by mutableStateOf("")
        private set
    override var nameIsNotValid: Boolean by mutableStateOf(false)
        private set
    override var date: LocalDate by mutableStateOf(LocalDate.fromEpochDays(0))
        private set

    val _state = MutableStateFlow<AddEventState>(AddEventState.Initial)
    override val state: StateFlow<AddEventState> = _state

    val _refresh = MutableSharedFlow<Boolean>()
    override val refresh: SharedFlow<Boolean> = _refresh

    override fun setExistsId(value: EventId?) {
        existsId = value
    }

    override fun updateName(value: String) {
        name = value
        nameIsNotValid = false
    }

    override fun updateDate(value: LocalDate) {
        date = value
    }

    override fun submit() {
        if (state == EventsState.Loading) {
            return
        }

        name = name.trim()
        nameIsNotValid = name.isEmpty()
        if (nameIsNotValid) {
            return
        }

        viewModelScope.launch {
            try {
                _state.emit(AddEventState.InProgress)
                _refresh.emit(false)

                val event = Event(
                    id = existsId,
                    title = name,
                    date = date.toTimestamp()
                )
                if (event.id != null) {
                    eventRepo.update(event)
                } else {
                    eventRepo.add(event)
                }
                clear()
                _state.emit(AddEventState.Success)
                _refresh.emit(true)
            } catch (e: Throwable) {
                Log.e(
                    "AddEventViewModel",
                    "Unable to add or change event",
                    e
                )
                _state.emit(AddEventState.Failed(e))
            }
        }
    }

    override fun clear() {
        name = ""
        nameIsNotValid = false
        date = LocalDate.fromEpochDays(0)
        existsId = null
    }
}