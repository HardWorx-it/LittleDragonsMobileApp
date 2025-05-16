package com.example.littledragons.ui.events.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littledragons.model.service.EventsRepository
import com.example.littledragons.model.types.Event
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IEventsViewModel {
    val state: StateFlow<EventsState>
    val deleteResult: StateFlow<DeleteEventResult>
    fun setDateRange(range: Pair<Timestamp, Timestamp>)
    fun load(force: Boolean = false)
    fun delete(event: Event)
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepo: EventsRepository
) : ViewModel(), IEventsViewModel {
    private var dateRange: Pair<Timestamp, Timestamp> = Timestamp(0, 0) to Timestamp(0, 0)

    val _state = MutableStateFlow<EventsState>(EventsState.Initial)
    override val state: StateFlow<EventsState> = _state

    val _deleteResult = MutableStateFlow<DeleteEventResult>(DeleteEventResult.Initial)
    override val deleteResult: StateFlow<DeleteEventResult> = _deleteResult

    override fun setDateRange(range: Pair<Timestamp, Timestamp>) {
        dateRange = range
    }

    override fun load(force: Boolean) {
        if (!force && (state.value == EventsState.Loading || state.value is EventsState.Loaded)) {
            return
        }

        viewModelScope.launch {
            _state.emit(EventsState.Loading)

            try {
                _state.emit(
                    EventsState.Loaded(
                        eventRepo.getAll(dateRange)
                            .sortedByDescending { it.date })
                )
            } catch (e: Throwable) {
                Log.e(
                    "EventsViewModel",
                    "Unable to fetch events list",
                    e
                )
                _state.emit(EventsState.Failed(e))
            }
        }
    }

    override fun delete(event: Event) {
        viewModelScope.launch {
            _deleteResult.emit(DeleteEventResult.InProgress)

            try {
                removeFromList(event)
                eventRepo.delete(event)
                _deleteResult.emit(DeleteEventResult.Success)
            } catch (e: Throwable) {
                Log.e(
                    "EventsViewModel",
                    "Unable to delete event",
                    e
                )
                returnToList(event)
                _deleteResult.emit(DeleteEventResult.Failed(e))
            }
        }
    }

    private suspend fun removeFromList(event: Event) {
        when (val s = _state.value) {
            is EventsState.Loaded -> _state.emit(EventsState.Loaded(s.events.filter { event != it }))
            else -> {}
        }
    }

    private suspend fun returnToList(event: Event) {
        when (val s = _state.value) {
            is EventsState.Loaded -> _state.emit(
                EventsState.Loaded(
                    (s.events + event).sortedByDescending { it.date })
            )

            else -> {}
        }
    }
}