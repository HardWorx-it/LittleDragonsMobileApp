package com.example.littledragons.ui.events.model

import com.example.littledragons.model.types.Event

sealed interface EventsState {
    data object Initial : EventsState
    data object Loading : EventsState
    data class Loaded(val events: List<Event>) : EventsState
    data class Failed(val error: Throwable) : EventsState
}
