package com.example.littledragons.model.service

import com.example.littledragons.model.types.Event
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface EventsRepository {
    suspend fun add(event: Event)

    suspend fun update(event: Event)

    suspend fun delete(event: Event)

    suspend fun getAll(dateRange: Pair<Timestamp, Timestamp>): List<Event>
}

class EventsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : EventsRepository {
    private val events
        get() = db.collection("events")

    override suspend fun add(event: Event) {
        events.add(event).await()
    }

    override suspend fun update(event: Event) {
        events.document(event.id!!).set(event).await()
    }

    override suspend fun delete(event: Event) {
        events.document(event.id!!).delete().await()
    }

    override suspend fun getAll(dateRange: Pair<Timestamp, Timestamp>): List<Event> {
        return events
            .whereGreaterThanOrEqualTo("date", dateRange.first)
            .whereLessThanOrEqualTo("date", dateRange.second)
            .get()
            .await().toObjects()
    }
}