package com.example.diaryapp.data.source

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser =
        auth.signInWithEmailAndPassword(email, password).await().user!!

    suspend fun signUp(email: String, password: String): FirebaseUser =
        auth.createUserWithEmailAndPassword(email, password).await().user!!

    fun signOut() = auth.signOut()

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
