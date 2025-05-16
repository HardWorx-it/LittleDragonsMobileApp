package com.example.littledragons.model.service

import com.example.littledragons.model.types.Schedule
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.UserId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface SchedulesRepository {
    suspend fun add(schedule: Schedule)

    suspend fun update(schedule: Schedule)

    suspend fun delete(schedule: Schedule)

    suspend fun get(
        schoolClassId: SchoolClassId,
        timeRange: Pair<Timestamp, Timestamp>
    ): List<Schedule>

    suspend fun getByTeacher(
        teacherId: UserId,
        schoolClassId: SchoolClassId,
        timeRange: Pair<Timestamp, Timestamp>
    ): List<Schedule>
}

class SchedulesRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : SchedulesRepository {
    private val schedules
        get() = db.collection("schedules")

    override suspend fun add(schedule: Schedule) {
        schedules.add(schedule).await()
    }

    override suspend fun update(schedule: Schedule) {
        schedules.document(schedule.id!!).set(schedule).await()
    }

    override suspend fun delete(schedule: Schedule) {
        schedules.document(schedule.id!!).delete().await()
    }

    override suspend fun get(
        schoolClassId: SchoolClassId,
        timeRange: Pair<Timestamp, Timestamp>
    ): List<Schedule> {
        return schedules
            .whereEqualTo("classId", schoolClassId)
            .whereGreaterThanOrEqualTo("startTime", timeRange.first)
            .whereLessThan("startTime", timeRange.second)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .get()
            .await().toObjects()
    }

    override suspend fun getByTeacher(
        teacherId: UserId,
        schoolClassId: SchoolClassId,
        timeRange: Pair<Timestamp, Timestamp>
    ): List<Schedule> {
        return schedules
            .whereEqualTo("teacherId", teacherId)
            .whereEqualTo("classId", schoolClassId)
            .whereGreaterThanOrEqualTo("startTime", timeRange.first)
            .whereLessThan("startTime", timeRange.second)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .get()
            .await().toObjects()
    }
}