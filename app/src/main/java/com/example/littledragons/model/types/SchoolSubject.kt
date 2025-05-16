package com.example.littledragons.model.types

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

typealias SchoolSubjectId = String

@Serializable
data class SchoolSubject(
    @DocumentId
    val name: SchoolSubjectId? = null
)
