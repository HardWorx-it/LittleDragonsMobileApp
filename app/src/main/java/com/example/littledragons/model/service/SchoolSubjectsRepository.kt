package com.example.littledragons.model.service

import com.example.littledragons.model.types.SchoolSubject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SchoolSubjectsRepository {
    suspend fun add(subject: SchoolSubject)

    fun listenAll(): Flow<List<SchoolSubject>>
}

@Singleton
class SchoolSubjectsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : SchoolSubjectsRepository {
    private val schoolSubjects
        get() = db.collection("subjects")

    override suspend fun add(subject: SchoolSubject) {
        schoolSubjects.document(subject.name!!).set(subject).await()
    }

    override fun listenAll(): Flow<List<SchoolSubject>> =
        schoolSubjects.snapshots().map { it.toObjects() }
}