package com.example.diaryapp.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut()
    // Design Ref: joyary-login-biometric §1.2 — 동기 signOut (MainActivity 에서 직접 호출)
    fun signOutImmediate()
    fun isLoggedIn(): Boolean
    // Design Ref: joyary-login-biometric §2.3 — 생체인증 자격증명 관리
    fun saveCredentials(email: String, password: String)
    fun hasBiometricCredentials(): Boolean
    fun getBiometricCredentials(): Pair<String, String>?
}
