package com.example.diaryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.AuthRepository
import com.example.diaryapp.ui.auth.AuthUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()
    val currentUserId: String get() = authRepository.getCurrentUser()?.uid ?: ""

    val currentUserIdFlow: StateFlow<String> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid ?: "")
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        awaitClose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, currentUserId)
    // Design Ref: joyary-login-biometric §2.4 — 저장된 생체 자격증명 여부
    val hasBiometricCredentials: Boolean get() = authRepository.hasBiometricCredentials()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signIn(email, password)
                .onSuccess {
                    // Design Ref: joyary-login-biometric §2.4 — 로그인 성공 시 자격증명 저장
                    authRepository.saveCredentials(email, password)
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "로그인 실패") }
        }
    }

    // Design Ref: joyary-login-biometric §2.4 — 생체인증 성공 후 저장된 자격증명으로 로그인
    fun signInWithBiometric() {
        val (email, password) = authRepository.getBiometricCredentials() ?: run {
            _uiState.value = AuthUiState.Error("저장된 인증 정보가 없습니다")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signIn(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "생체 인증 로그인 실패") }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("비밀번호가 일치하지 않습니다")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUp(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "회원가입 실패") }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    // Design Ref: joyary-login-biometric §1.2 — 앱 시작 시 동기 즉시 signOut
    fun signOutImmediate() {
        authRepository.signOutImmediate()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
