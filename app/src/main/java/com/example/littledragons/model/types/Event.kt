package com.example.littledragons.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

typealias EventId = String

@Serializable
data class Event(
    @DocumentId
    val id: EventId? = null,
    val title: String? = null,
    @ServerTimestamp
    @Contextual
    val date: Timestamp? = null
)
