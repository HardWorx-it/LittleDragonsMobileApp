package com.example.littledragons.model.service

import com.example.littledragons.model.types.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface NotificationsRepository {
    suspend fun add(event: Notification)

    suspend fun delete(event: Notification)

    suspend fun getAll(): List<Notification>
}

class NotificationsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : NotificationsRepository {
    private val notifications
        get() = db.collection("notifications")

    override suspend fun add(event: Notification) {
        notifications.add(event).await()
    }

    override suspend fun delete(event: Notification) {
        notifications.document(event.id!!).delete().await()
    }

    override suspend fun getAll(): List<Notification> = notifications
        .orderBy("timestamp")
        .get()
        .await().toObjects()
}