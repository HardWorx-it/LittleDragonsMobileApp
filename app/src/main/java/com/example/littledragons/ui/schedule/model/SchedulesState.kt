package com.example.littledragons.ui.schedule.model

import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserId
import com.google.firebase.Timestamp

sealed interface SchedulesState {
    val currentUser: UserRole?

    data object Initial : SchedulesState {
        override val currentUser: UserRole? = null
    }

    data class Loading(
        override val currentUser: UserRole,
        val timeRange: Pair<Timestamp, Timestamp>,
    ) : SchedulesState

    data class Loaded(
        override val currentUser: UserRole,
        val schedules: List<Schedule>,
        val timeRange: Pair<Timestamp, Timestamp>,
    ) : SchedulesState

    sealed interface Failed : SchedulesState {
        data object ChildNotFound : Failed {
            override val currentUser: UserRole? = null
        }

        data class Error(
            override val currentUser: UserRole,
            val error: Throwable,
            val timeRange: Pair<Timestamp, Timestamp>,
        ) : Failed
    }

    sealed interface UserRole {
        data class Parent(val child: Student?) : UserRole
        data class Teacher(
            val teacherId: UserId,
            val schoolClassId: SchoolClassId
        ) : UserRole
    }
}