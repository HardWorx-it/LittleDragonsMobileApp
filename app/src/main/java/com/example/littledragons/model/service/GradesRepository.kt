package com.example.littledragons.model.service

import com.example.littledragons.model.types.Grade
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.SchoolSubjectId
import com.example.littledragons.model.types.StudentId
import com.example.littledragons.model.types.UserId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface GradesRepository {
    suspend fun add(grade: Grade)

    suspend fun update(grade: Grade)

    suspend fun delete(grade: Grade)

    suspend fun getByTeacher(
        teacherId: UserId,
        schoolClassId: SchoolClassId,
        subjectId: SchoolSubjectId,
    ): List<Grade>

    suspend fun getByStudent(
        studentId: StudentId,
        subjectId: SchoolSubjectId,
    ): List<Grade>
}

class GradesRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : GradesRepository {
    private val grades
        get() = db.collection("grades")

    override suspend fun add(grade: Grade) {
        grades.add(grade).await()
    }

    override suspend fun update(grade: Grade) {
        grades.document(grade.id!!).set(grade).await()
    }

    override suspend fun delete(grade: Grade) {
        grades.document(grade.id!!).delete().await()
    }

    override suspend fun getByTeacher(
        teacherId: UserId,
        schoolClassId: SchoolClassId,
        subjectId: SchoolSubjectId,
    ): List<Grade> {
        return grades
            .whereEqualTo("teacherId", teacherId)
            .whereEqualTo("classId", schoolClassId)
            .whereEqualTo("subjectId", subjectId)
            .orderBy("date")
            .get()
            .await().toObjects()
    }

    override suspend fun getByStudent(
        studentId: StudentId,
        subjectId: SchoolSubjectId,
    ): List<Grade> {
        return grades
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("subjectId", subjectId)
            .orderBy("date")
            .get()
            .await().toObjects()
    }
}