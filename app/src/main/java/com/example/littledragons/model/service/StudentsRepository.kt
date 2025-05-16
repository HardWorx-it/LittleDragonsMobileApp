package com.example.littledragons.model.service

import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.StudentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface StudentsRepository {
    suspend fun add(student: Student)

    suspend fun getById(id: StudentId): Student?

    suspend fun find(
        firstName: String,
        lastName: String,
        schoolClassId: SchoolClassId
    ): List<Student>?

    fun listenAll(): Flow<List<Student>>
}

@Singleton
class StudentsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : StudentsRepository {
    private val students
        get() = db.collection("students")

    override suspend fun add(student: Student) {
        students.add(student).await()
    }

    override suspend fun getById(id: StudentId): Student? =
        students.document(id).get().await().toObject()

    override suspend fun find(
        firstName: String,
        lastName: String,
        schoolClassId: SchoolClassId
    ): List<Student>? =
        students
            .whereEqualTo("firstName", firstName)
            .whereEqualTo("lastName", lastName)
            .whereEqualTo("classId", schoolClassId)
            .get().await().toObjects()

    override fun listenAll(): Flow<List<Student>> = students.snapshots().map { it.toObjects() }
}