package com.example.littledragons.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

typealias NotificationId = String

@Serializable
data class Notification(
    @DocumentId
    val id: NotificationId? = null,
    val title: String? = null,
    @ServerTimestamp
    @Contextual
    val timestamp: Timestamp? = null
)
