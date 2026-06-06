package kr.pe.dongha.flash2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashViewModel(application: Application) : AndroidViewModel(application) {
    private val flashController = FlashController(application.applicationContext)
    private val _uiState = MutableStateFlow(
        FlashUiState(isFlashAvailable = flashController.isFlashAvailable)
    )

    val uiState: StateFlow<FlashUiState> = _uiState.asStateFlow()

    private var sosJob: Job? = null
    private var timerJob: Job? = null

    init {
        flashController.registerTorchCallback(
            onChanged = { enabled ->
                _uiState.update { it.copy(isFlashOn = enabled) }
            },
            onUnavailable = {
                _uiState.update {
                    it.copy(
                        isFlashOn = false,
                        errorMessage = "플래시를 사용할 수 없습니다. 카메라가 사용 중일 수 있습니다."
                    )
                }
            }
        )
    }

    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun onCameraPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasCameraPermission = granted,
                errorMessage = if (granted) null else "플래시 사용을 위해 카메라 권한이 필요합니다."
            )
        }
    }

    fun toggleFlash() {
        if (!canUseFlash()) return
        if (_uiState.value.isSosRunning) stopSos()

        setFlash(!_uiState.value.isFlashOn)
    }

    fun toggleScreenLight() {
        val turningOn = !_uiState.value.isScreenLightOn
        if (turningOn) {
            stopSos()
            setFlash(false)
        }

        _uiState.update {
            it.copy(
                isScreenLightOn = turningOn,
                errorMessage = null
            )
        }
    }

    fun toggleSos() {
        if (_uiState.value.isSosRunning) {
            stopSos()
        } else {
            startSos()
        }
    }

    fun selectTimer(option: TimerOption) {
        if (!canUseFlash()) return

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                selectedTimer = option,
                timerRemainingSeconds = (option.durationMillis / 1_000L).toInt(),
                errorMessage = null
            )
        }

        if (!_uiState.value.isFlashOn) {
            setFlash(true)
        }

        timerJob = viewModelScope.launch {
            var remainingSeconds = (option.durationMillis / 1_000L).toInt()
            while (isActive && remainingSeconds > 0) {
                delay(1_000L)
                remainingSeconds -= 1
                _uiState.update { it.copy(timerRemainingSeconds = remainingSeconds) }
            }

            if (isActive) {
                setFlash(false)
                _uiState.update {
                    it.copy(selectedTimer = null, timerRemainingSeconds = null)
                }
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(selectedTimer = null, timerRemainingSeconds = null) }
    }

    fun stopAll() {
        stopSos()
        cancelTimer()
        setFlash(false)
        _uiState.update { it.copy(isScreenLightOn = false) }
    }

    override fun onCleared() {
        stopAll()
        flashController.unregisterTorchCallback()
        super.onCleared()
    }

    private fun startSos() {
        if (!canUseFlash()) return

        cancelTimer()
        _uiState.update {
            it.copy(
                isSosRunning = true,
                isScreenLightOn = false,
                errorMessage = null
            )
        }

        sosJob = viewModelScope.launch {
            while (isActive) {
                playSosPattern()
                delay(700L)
            }
        }
    }

    private suspend fun playSosPattern() {
        repeat(3) { blink(180L) }
        delay(250L)
        repeat(3) { blink(500L) }
        delay(250L)
        repeat(3) { blink(180L) }
    }

    private suspend fun blink(durationMillis: Long) {
        setFlash(true)
        delay(durationMillis)
        setFlash(false)
        delay(180L)
    }

    private fun stopSos() {
        sosJob?.cancel()
        sosJob = null
        setFlash(false)
        _uiState.update { it.copy(isSosRunning = false) }
    }

    private fun canUseFlash(): Boolean {
        val state = _uiState.value
        val message = when {
            !state.isFlashAvailable -> "이 기기는 플래시를 지원하지 않습니다."
            !state.hasCameraPermission -> "플래시 사용을 위해 카메라 권한이 필요합니다."
            else -> null
        }

        if (message != null) {
            _uiState.update { it.copy(errorMessage = message) }
            return false
        }

        return true
    }

    private fun setFlash(enabled: Boolean): Boolean {
        val result = flashController.setFlash(enabled)
        val errorMessage = when (result) {
            FlashResult.Success -> null
            FlashResult.NotSupported -> "이 기기는 플래시를 지원하지 않습니다."
            FlashResult.PermissionDenied -> "카메라 권한이 거부되어 플래시를 켤 수 없습니다."
            FlashResult.Unavailable -> "플래시를 사용할 수 없습니다. 카메라가 사용 중일 수 있습니다."
        }

        _uiState.update {
            it.copy(
                isFlashOn = if (result == FlashResult.Success) enabled else false,
                errorMessage = errorMessage
            )
        }

        return result == FlashResult.Success
    }
}
