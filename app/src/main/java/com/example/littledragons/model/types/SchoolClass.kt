package com.example.littledragons.model.types

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

typealias SchoolClassId = String

@Serializable
data class SchoolClass(
    @DocumentId
    val name: SchoolClassId? = null,
)