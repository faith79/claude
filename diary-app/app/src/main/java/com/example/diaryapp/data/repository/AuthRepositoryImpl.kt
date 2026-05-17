package com.example.diaryapp.data.repository

import com.example.diaryapp.data.source.AuthDataSource
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {
    override fun getCurrentUser(): FirebaseUser? = authDataSource.getCurrentUser()
    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        runCatching { authDataSource.signIn(email, password) }
    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        runCatching { authDataSource.signUp(email, password) }
    override suspend fun signOut() = authDataSource.signOut()
    override fun isLoggedIn(): Boolean = authDataSource.isLoggedIn()
}
