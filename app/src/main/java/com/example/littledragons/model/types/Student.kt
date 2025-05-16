package com.example.littledragons.model.types

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

typealias StudentId = String

@Serializable
data class Student(
    @DocumentId
    val id: StudentId? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val classId: SchoolClassId? = null
)

val Student.name: String
    get() = "$firstName $lastName"