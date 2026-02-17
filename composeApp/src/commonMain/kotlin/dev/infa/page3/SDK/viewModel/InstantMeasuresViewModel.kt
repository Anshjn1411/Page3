package dev.infa.page3.SDK.viewModel

import dev.infa.page3.SDK.data.OneClickResult
import dev.infa.page3.SDK.data.RawDataResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InstantMeasuresViewModel(
    private val syncManager: IInstantMeasures
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun measureHeartRateOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureHeartRate()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun measureSpO2Once(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureSpO2()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun measureHrvOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureHrv()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun measureBloodPressureOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureBloodPressure()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun measurePressureOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measurePressure()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    fun measureTemperatureOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureTemperature()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("Error: ${e.message}")
                }
            }
        }
    }

    // ============ ONE-KEY WITH CONTINUOUS UPDATES ============
    fun measureOneClickOnce(
        onUpdate: (OneClickResult) -> Unit,  // Called multiple times as data updates
        onComplete: () -> Unit,              // Called when measurement is done
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    syncManager.measureOneClickContinuous(
                        onUpdate = { result ->
                            // Update UI every time device sends new data

                            onUpdate(result)

                        },
                        onComplete = {

                            onComplete()

                        }

                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    fun measureHeartRateRawDataOnce(seconds: Int, onResult: (RawDataResult) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureHeartRateRawData(seconds)
                    onResult(result)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    fun measureBloodOxygenRawDataOnce(seconds: Int, onResult: (RawDataResult) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val result = syncManager.measureBloodOxygenRawData(seconds)
                    onResult(result)
                } catch (e: Exception) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }
}
