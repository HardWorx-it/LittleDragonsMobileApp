package com.example.littledragons.model.service

import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AuthServiceTest {
    private lateinit var authService: AuthService
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepo: UserRepository

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        auth = mockk()
        userRepo = mockk()

        every { auth.currentUser } returns mockk()
        every { auth.addAuthStateListener(any()) } just runs

        authService = AuthServiceImpl(auth, userRepo)

    }

    @Test
    fun isAuthorized() {
        Assert.assertTrue(authService.isAuthorized())
        every { auth.currentUser } returns null
        Assert.assertFalse(authService.isAuthorized())
    }

    @Test
    fun createUserWithEmailAndPassword() = runTest {
        val uid = "1"
        val email = "email"
        val password = "password"
        val firstName = "first"
        val lastName = "last"
        val role = UserRole.Parent
        val userAccount = UserAccount(
            uid = uid,
            email = email,
            role = role,
            firstName = firstName,
            lastName = lastName,
            childId = null,
        )

        val result = mockk<AuthResult>()
        val user = mockk<FirebaseUser>()
        every { result.user } returns user
        every { user.uid } returns uid
        coEvery { auth.createUserWithEmailAndPassword(email, password).await() } returns result
        coEvery { userRepo.add(userAccount) } just runs

        authService.createUserWithEmailAndPassword(email, password, firstName, lastName, role)
        coVerify { auth.createUserWithEmailAndPassword(email, password).await() }
        coVerify { userRepo.add(userAccount) }
    }

    @Test
    fun signInWithEmailAndPassword() = runTest {
        val email = "email"
        val password = "password"

        coEvery { auth.signInWithEmailAndPassword(email, password).await() } returns mockk()

        authService.signInWithEmailAndPassword(email, password)
        coVerify { auth.signInWithEmailAndPassword(email, password).await() }
    }

    @Test
    fun `createUserWithEmailAndPassword (User not found)`() = runTest {
        val email = "email"
        val password = "password"
        val firstName = "first"
        val lastName = "last"
        val role = UserRole.Parent

        val result = mockk<AuthResult>()
        every { result.user } returns null
        coEvery { auth.createUserWithEmailAndPassword(email, password).await() } returns result

        runCatching {
            authService.createUserWithEmailAndPassword(
                email,
                password,
                firstName,
                lastName,
                role
            )
        }.onFailure {
            Assert.assertTrue(it is Exception)
            coVerify { auth.createUserWithEmailAndPassword(email, password).await() }
        }
    }
}