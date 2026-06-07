package com.example.diaryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.model.MemoEntry
import com.example.diaryapp.data.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _memos = MutableStateFlow<List<MemoEntry>>(emptyList())
    val memos: StateFlow<List<MemoEntry>> = _memos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMemos(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _memos.value = memoRepository.getMemos(userId)
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveMemo(userId: String, memo: MemoEntry) {
        viewModelScope.launch {
            try {
                memoRepository.saveMemo(userId, memo)
                loadMemos(userId)
            } catch (_: Exception) {
            }
        }
    }

    fun deleteMemo(userId: String, memoId: String) {
        viewModelScope.launch {
            try {
                memoRepository.deleteMemo(userId, memoId)
                _memos.value = _memos.value.filter { it.id != memoId }
            } catch (_: Exception) {
            }
        }
    }
}
