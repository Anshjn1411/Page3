package dev.infa.page3.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {
    companion object {
        private const val TAG = "QCRingVM"
    }
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    fun addLog(message: String) {
        viewModelScope.launch {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val newLog = "[$timestamp] $message"
            val updated = listOf(newLog) + _logMessages.value
            _logMessages.value = if (updated.size > 200) updated.take(200) else updated
            Log.d(TAG, newLog)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            _logMessages.value = emptyList()
        }
    }
}