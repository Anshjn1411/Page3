package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.StartEndTimeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContinuousMonitoringViewModel(
    private val syncManager: IContinuousMonitoring
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun toggleHeartRateMonitoring(enabled: Boolean, interval: Int = 30, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleHeartRateMonitoring(enabled, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun toggleHrvMonitoring(enabled: Boolean, interval: Int = 0, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleHrvMonitoring(enabled, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun toggleSpO2Monitoring(enabled: Boolean, interval: Int = 0, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleSpO2Monitoring(enabled, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun toggleIntervalSpO2Monitoring(enabled: Boolean, interval: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleIntervalSpO2Monitoring(enabled, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun toggleBloodPressureMonitoring(
        enabled: Boolean,
        startEndTime: StartEndTimeEntity,
        interval: Int = 60,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleBloodPressureMonitoring(enabled, startEndTime, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun togglePressureMonitoring(enabled: Boolean, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.togglePressureMonitoring(enabled)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun toggleTemperatureMonitoring(enabled: Boolean, interval: Int = 30, onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.toggleTemperatureMonitoring(enabled, interval)
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }
}