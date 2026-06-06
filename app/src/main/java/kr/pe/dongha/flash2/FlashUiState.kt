package kr.pe.dongha.flash2

data class FlashUiState(
    val isFlashOn: Boolean = false,
    val isScreenLightOn: Boolean = false,
    val isSosRunning: Boolean = false,
    val selectedTimer: TimerOption? = null,
    val errorMessage: String? = null,
    val isFlashAvailable: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val timerRemainingSeconds: Int? = null
)

enum class TimerOption(
    val label: String,
    val durationMillis: Long
) {
    OneMinute("1분", 60_000L),
    ThreeMinutes("3분", 180_000L),
    FiveMinutes("5분", 300_000L)
}
