package dev.infa.page3.viewmodels



import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oudmon.ble.base.bean.SleepDisplay
import com.oudmon.ble.base.communication.rsp.SleepNewProtoResp

import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.BloodOxygenEntity
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.req.HRVReq
import com.oudmon.ble.base.communication.req.HrvSettingReq
import com.oudmon.ble.base.communication.req.ReadPressureReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BloodOxygenSettingRsp
import com.oudmon.ble.base.communication.rsp.BpSettingRsp
import com.oudmon.ble.base.communication.rsp.HRVRsp
import com.oudmon.ble.base.communication.rsp.ReadBlePressureRsp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class HrvViewModel(
    private val cacheManager: HealthMetricsCacheManager
) : ViewModel()
{

    private val _hrvData = MutableStateFlow<HrvData?>(null)
    val hrvData: StateFlow<HrvData?> = _hrvData.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun measureHrvOnce(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.i("HrvVM", "Starting manual HRV measurement...")
                BleOperateManager.getInstance().manualModeHrv({ result ->
                    val hrvValue = result.value
                    val errCode = result.errCode.toInt()
                    if (errCode == 0 && hrvValue > 0) {
                        Log.i("HrvVM", "Manual HRV measured: $hrvValue ms")
                        onResult("HRV: $hrvValue ms")
                    } else {
                        onResult("Measurement failed or invalid")
                    }
                }, false)
            } catch (e: Exception) {
                onResult("Exception: ${e.message}")
            }
        }
    }

    fun syncHrvDataForDay(
        offset: Int,
        onSuccess: (HrvData) -> Unit,
        onError: (String) -> Unit,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (!forceRefresh) {
                cacheManager.getData<HrvData>("hrv", offset)?.let { cached ->
                    _hrvData.value = cached
                    onSuccess(cached)
                    Log.d("HrvVM", "✅ Using cached HRV data for offset $offset")
                }
            }

            _isSyncing.value = true
            withContext(Dispatchers.IO) {
                try {
                    CommandHandle.getInstance().executeReqCmd(
                        HRVReq(offset.toByte()),
                        object : ICommandResponse<HRVRsp> {
                            override fun onDataResponse(resultEntity: HRVRsp) {
                                if (resultEntity.range > 0) {
                                    val hrvData = convertHrvData(resultEntity, offset)
                                    _hrvData.value = hrvData
                                    onSuccess(hrvData)
                                    cacheManager.saveData("hrv", offset, hrvData)
                                    Log.d("HrvVM", "✅ Synced HRV data for offset $offset")
                                } else {
                                    onError("Failed to sync HRV data")
                                }
                                _isSyncing.value = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    onError("Exception: ${e.message}")
                    _isSyncing.value = false
                }
            }
        }
    }

    fun toggleHrvMonitoring(enabled: Boolean, onComplete: () -> Unit) {
        try {
            CommandHandle.getInstance().executeReqCmd(HrvSettingReq(enabled), null)
            Log.d("HrvVM", "HRV monitoring ${if (enabled) "enabled" else "disabled"}")
            onComplete()
        } catch (e: Exception) {
            Log.e("HrvVM", "Exception: ${e.message}")
        }
    }

    private fun convertHrvData(response: HRVRsp, offset: Int): HrvData {
        val hrvValues = mutableListOf<HrvEntry>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val baseTimestamp = calendar.timeInMillis / 1000

        response.hrvArray?.let { hrvArray ->
            val intervalMinutes = response.range.coerceAtLeast(1)
            for (i in hrvArray.indices) {
                val hrvValue = (hrvArray[i].toInt() and 0xFF)
                if (hrvValue > 0) {
                    hrvValues.add(
                        HrvEntry(
                            timestamp = baseTimestamp + (i * intervalMinutes * 60),
                            hrvValue = hrvValue,
                            minuteOfDay = i * intervalMinutes
                        )
                    )
                }
            }
        }

        val avgHrv = if (hrvValues.isNotEmpty())
            hrvValues.map { it.hrvValue }.average().toInt() else 0

        return HrvData(
            date = getDateForOffset(offset),
            hrvValues = hrvValues,
            averageHrv = avgHrv,
            maxHrv = hrvValues.maxOfOrNull { it.hrvValue } ?: 0,
            minHrv = hrvValues.minOfOrNull { it.hrvValue } ?: 0
        )
    }

    private fun getDateForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun forceRefresh(offset: Int, onSuccess: (HrvData) -> Unit, onError: (String) -> Unit) {
        cacheManager.clearMetricCache("hrv")
        syncHrvDataForDay(offset, onSuccess, onError, forceRefresh = true)
    }
}

// ========================================
// DATA CLASSES
// ========================================

data class HrvData(
    val date: String = "",
    val hrvValues: List<HrvEntry> = emptyList(),
    val averageHrv: Int = 0,
    val maxHrv: Int = 0,
    val minHrv: Int = 0
) {
    fun getFormattedAverageHrv(): String {
        return "$averageHrv ms"
    }

    fun getHrvStatus(): String {
        return when {
            averageHrv < 30 -> "Low"
            averageHrv < 60 -> "Normal"
            else -> "Good"
        }
    }
}

data class HrvEntry(
    val timestamp: Long = 0,
    val hrvValue: Int = 0,
    val minuteOfDay: Int = 0
)

