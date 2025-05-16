package com.example.littledragons.ui.grades.model

import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.StudentId
import com.example.littledragons.model.types.UserId

sealed interface GradesState {
    val currentSubject: SchoolSubjectId?
    val currentUser: UserRole?

    data object Initial : GradesState {
        override val currentSubject: SchoolSubjectId? = null
        override val currentUser: UserRole? = null
    }

    data class Loading(
        override val currentUser: UserRole,
        override val currentSubject: SchoolSubjectId,
    ) : GradesState

    data class Loaded(
        val grades: List<GradeItem>,
        override val currentUser: UserRole,
        override val currentSubject: SchoolSubjectId,
    ) : GradesState

    sealed interface Failed : GradesState {
        data class ChildNotFound(
            override val currentUser: UserRole? = null,
            override val currentSubject: SchoolSubjectId,
        ) : Failed

        data class Error(
            val error: Throwable,
            override val currentUser: UserRole,
            override val currentSubject: SchoolSubjectId,
        ) : Failed
    }

    sealed interface UserRole {
        data class Parent(val childId: StudentId?) : UserRole
        data class Teacher(
            val teacherId: UserId,
            val schoolClassId: SchoolClassId
        ) : UserRole
    }
}