package com.example.littledragons.model.types

import androidx.annotation.IntRange
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

typealias GradeId = String

@Serializable
data class Grade(
    @DocumentId
    val id: GradeId? = null,
    val teacherId: UserId? = null,
    val classId: SchoolClassId? = null,
    val studentId: StudentId? = null,
    val subjectId: SchoolSubjectId? = null,
    @get:IntRange(from = 2, to = 5)
    val gradeValue: Int? = null,
    @ServerTimestamp
    @Contextual
    val date: Timestamp? = null
)
