package com.example.littledragons.model.types

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    Parent,
    Teacher
}

typealias UserId = String

@Serializable
data class UserAccount(
    val uid: UserId? = null,
    val email: String? = null,
    val role: UserRole? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val childId: StudentId? = null,
    val avatar: String? = null // base64 для Firestore
)
