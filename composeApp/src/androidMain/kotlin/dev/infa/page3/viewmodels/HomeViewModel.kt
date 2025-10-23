package dev.infa.page3.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.DeviceManager
import dev.infa.page3.models.DeviceCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val dataSynchronization: DataSynchronization,
    val deviceCapabilities: DeviceCapabilities
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _todayStep = MutableStateFlow<StepData?>(null)
    val todayStep: StateFlow<StepData?> = _todayStep.asStateFlow()

    private val _todaySleep = MutableStateFlow<SleepData?>(null)
    val todaySleep: StateFlow<SleepData?> = _todaySleep.asStateFlow()

    private val _latestHeart = MutableStateFlow<HeartRateData?>(null)
    val latestHeart: StateFlow<HeartRateData?> = _latestHeart.asStateFlow()

    init {
        setupCallbacks()
        refreshAll()
    }

    private fun setupCallbacks() {
        dataSynchronization.setStepDataCallback { step ->
            _todayStep.value = step
        }
        dataSynchronization.setSleepDataCallback { sleep ->
            _todaySleep.value = sleep
        }
        dataSynchronization.setHeartRateCallback { heart ->
            _latestHeart.value = heart
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                withContext(Dispatchers.IO) {
                    // Steps (today)
                    dataSynchronization.syncTodaySteps()

                    // Sleep (today = offset 0)
                    val address = getActualDeviceAddress()
                    if (address.isNotEmpty()) {
                        dataSynchronization.syncSleepData(address, 0)
                    }

                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getActualDeviceAddress(): String {
        return DeviceManager.getInstance().deviceAddress.ifEmpty { "" }
    }
}
