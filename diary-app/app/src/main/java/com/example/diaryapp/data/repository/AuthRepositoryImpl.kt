package com.example.diaryapp.data.repository

import com.example.diaryapp.data.source.AuthDataSource
import com.example.diaryapp.security.CredentialStorage
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val credentialStorage: CredentialStorage
) : AuthRepository {
    override fun getCurrentUser(): FirebaseUser? = authDataSource.getCurrentUser()
    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        runCatching { authDataSource.signIn(email, password) }
    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        runCatching { authDataSource.signUp(email, password) }
    override suspend fun signOut() = authDataSource.signOut()
    override fun signOutImmediate() = authDataSource.signOut()
    override fun isLoggedIn(): Boolean = authDataSource.isLoggedIn()
    override fun saveCredentials(email: String, password: String) =
        credentialStorage.saveCredentials(email, password)
    override fun hasBiometricCredentials(): Boolean = credentialStorage.hasCredentials()
    override fun getBiometricCredentials(): Pair<String, String>? {
        val email = credentialStorage.getEmail() ?: return null
        val password = credentialStorage.getPassword() ?: return null
        return email to password
    }
}
