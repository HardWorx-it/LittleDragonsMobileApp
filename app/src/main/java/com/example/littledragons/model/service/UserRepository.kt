package com.example.littledragons.model.service

import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun add(user: UserAccount)

    suspend fun getByUid(uid: UserId): UserAccount?

    suspend fun update(user: UserAccount)
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {
    private val users
        get() = db.collection("users")

    override suspend fun add(user: UserAccount) {
        users.document(user.uid!!).set(user).await()
    }

    override suspend fun getByUid(uid: UserId): UserAccount? =
        users.document(uid).get().await().toObject()

    override suspend fun update(user: UserAccount) = add(user = user)
}