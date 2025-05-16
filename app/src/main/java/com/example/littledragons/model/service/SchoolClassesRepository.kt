package com.example.littledragons.model.service

import com.example.littledragons.model.types.SchoolClass
import com.example.littledragons.model.types.SchoolClassId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SchoolClassesRepository {
    suspend fun add(schoolClass: SchoolClass)

    suspend fun getById(id: SchoolClassId): SchoolClass?

    fun listenAll(): Flow<List<SchoolClass>>
}

@Singleton
class SchoolClassesRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : SchoolClassesRepository {
    private val schoolClasses
        get() = db.collection("classes")

    override suspend fun add(schoolClass: SchoolClass) {
        schoolClasses.document(schoolClass.name!!).set(schoolClass).await()
    }

    override suspend fun getById(id: SchoolClassId): SchoolClass? =
        schoolClasses.document(id).get().await().toObject()

    override fun listenAll(): Flow<List<SchoolClass>> =
        schoolClasses.snapshots().map { it.toObjects() }
}