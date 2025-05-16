package com.example.littledragons.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

typealias ScheduleId = String

@Serializable
data class Schedule(
    @DocumentId
    val id: ScheduleId? = null,
    val subjectId: SchoolSubjectId? = null,
    val classId: SchoolClassId? = null,
    val teacherId: UserId? = null,
    @ServerTimestamp
    @Contextual
    val startTime: Timestamp? = null,
    @ServerTimestamp
    @Contextual
    val endTime: Timestamp? = null
)