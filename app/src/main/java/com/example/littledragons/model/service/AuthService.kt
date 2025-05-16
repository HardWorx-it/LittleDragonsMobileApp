package com.example.littledragons.model.service

import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthService {
    fun isAuthorized(): Boolean

    fun isVerified(): Boolean

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole
    ): UserAccount

    suspend fun signInWithEmailAndPassword(email: String, password: String)

    fun getUserUid(): String?

    suspend fun updateEmail(email: String)

    fun signOut()

    suspend fun sendEmailVerification()

    suspend fun reload()

    fun addAuthStateListener(listener: () -> Unit)

    fun removeAuthStateListener(listener: () -> Unit)
}

@Singleton
class AuthServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepository
) : AuthService {
    private val listeners: MutableList<() -> Unit> = mutableListOf()

    init {
        auth.addAuthStateListener {
            for (listener in listeners) {
                listener()
            }
        }
    }

    override fun isAuthorized(): Boolean = auth.currentUser != null

    override fun isVerified(): Boolean = auth.currentUser?.isEmailVerified == true

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole
    ): UserAccount {
        val result = auth.createUserWithEmailAndPassword(
            email,
            password
        ).await()
        return if (result.user == null) {
            throw Exception("User not found")
        } else {
            UserAccount(
                uid = result.user!!.uid,
                email = email,
                role = role,
                firstName = firstName,
                lastName = lastName,
            ).apply {
                userRepo.add(this)
            }
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(
            email,
            password
        ).await()
    }

    override fun getUserUid(): String? = auth.currentUser?.uid

    override suspend fun updateEmail(email: String) {
        auth.currentUser?.verifyBeforeUpdateEmail(email)?.await()
    }

    override fun signOut() = auth.signOut()

    override suspend fun sendEmailVerification() {
        if (!isVerified()) {
            auth.currentUser?.sendEmailVerification()?.await()
        }
    }

    override suspend fun reload() {
        auth.currentUser?.reload()?.await()
    }

    override fun addAuthStateListener(listener: () -> Unit) {
        listeners += listener
    }

    override fun removeAuthStateListener(listener: () -> Unit) {
        listeners -= listener
    }
}