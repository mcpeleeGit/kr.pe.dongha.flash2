package kr.pe.dongha.flash2

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlashScreen(
    viewModel: FlashViewModel,
    onRequestCameraPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    FlashTheme {
        FlashScreenContent(
            uiState = uiState,
            onToggleFlash = viewModel::toggleFlash,
            onToggleScreenLight = viewModel::toggleScreenLight,
            onToggleSos = viewModel::toggleSos,
            onSelectTimer = viewModel::selectTimer,
            onCancelTimer = viewModel::cancelTimer,
            onRequestCameraPermission = onRequestCameraPermission
        )
    }
}

@Composable
fun FlashScreenContent(
    uiState: FlashUiState,
    onToggleFlash: () -> Unit,
    onToggleScreenLight: () -> Unit,
    onToggleSos: () -> Unit,
    onSelectTimer: (TimerOption) -> Unit,
    onCancelTimer: () -> Unit,
    onRequestCameraPermission: () -> Unit
) {
    ApplyScreenBrightness(uiState.isScreenLightOn)

    if (uiState.isScreenLightOn) {
        ScreenLightMode(onToggleScreenLight)
        return
    }

    var showTimerDialog by remember { mutableStateOf(false) }
    val background = Color(0xFF111318)
    val accent = if (uiState.isFlashOn || uiState.isSosRunning) {
        Color(0xFFFFD54F)
    } else {
        Color(0xFF4B5563)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "간편 플래시",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(22.dp))
            StatusText(uiState)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onToggleFlash,
                enabled = !uiState.isSosRunning,
                modifier = Modifier.size(214.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = if (uiState.isFlashOn) Color(0xFF181818) else Color.White,
                    disabledContainerColor = Color(0xFF2D3340),
                    disabledContentColor = Color(0xFFB8C0CC)
                )
            ) {
                Text(
                    text = if (uiState.isFlashOn) "OFF" else "ON",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFFFB4AB),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            if (!uiState.hasCameraPermission && uiState.isFlashAvailable) {
                Text(
                    text = "후면 플래시를 켜고 끄기 위해 카메라 권한이 필요합니다.",
                    color = Color(0xFFE5E7EB),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onRequestCameraPermission) {
                    Text("권한 허용")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            BottomActions(
                uiState = uiState,
                onToggleScreenLight = onToggleScreenLight,
                onToggleSos = onToggleSos,
                onShowTimerDialog = { showTimerDialog = true }
            )
        }
    }

    if (showTimerDialog) {
        TimerDialog(
            uiState = uiState,
            onDismiss = { showTimerDialog = false },
            onSelectTimer = {
                onSelectTimer(it)
                showTimerDialog = false
            },
            onCancelTimer = {
                onCancelTimer()
                showTimerDialog = false
            }
        )
    }
}

@Composable
private fun StatusText(uiState: FlashUiState) {
    val status = when {
        !uiState.isFlashAvailable -> "이 기기는 플래시를 지원하지 않습니다."
        uiState.isSosRunning -> "SOS 실행 중"
        uiState.isFlashOn -> "플래시 켜짐"
        else -> "플래시 꺼짐"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = status,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        uiState.timerRemainingSeconds?.let { seconds ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "자동 꺼짐 ${seconds}초 남음",
                color = Color(0xFFFFD54F),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BottomActions(
    uiState: FlashUiState,
    onToggleScreenLight: () -> Unit,
    onToggleSos: () -> Unit,
    onShowTimerDialog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onToggleScreenLight,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("화면 조명")
        }
        OutlinedButton(
            onClick = onToggleSos,
            enabled = uiState.isFlashAvailable,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (uiState.isSosRunning) "SOS 중지" else "SOS")
        }
        OutlinedButton(
            onClick = onShowTimerDialog,
            enabled = uiState.isFlashAvailable,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("타이머")
        }
    }
}

@Composable
private fun TimerDialog(
    uiState: FlashUiState,
    onDismiss: () -> Unit,
    onSelectTimer: (TimerOption) -> Unit,
    onCancelTimer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("자동 꺼짐 타이머") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TimerOption.entries.forEach { option ->
                    Button(
                        onClick = { onSelectTimer(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(option.label)
                    }
                }
                if (uiState.selectedTimer != null) {
                    TextButton(
                        onClick = onCancelTimer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("타이머 취소")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

@Composable
private fun ScreenLightMode(onExit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
        onClick = onExit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onExit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111318),
                    contentColor = Color.White
                )
            ) {
                Text("화면 조명 종료")
            }
        }
    }
}

@Composable
private fun ApplyScreenBrightness(enabled: Boolean) {
    val activity = LocalContext.current.findActivity() ?: return

    DisposableEffect(enabled) {
        val window = activity.window
        val originalBrightness = window.attributes.screenBrightness
        if (enabled) {
            window.attributes = window.attributes.apply {
                screenBrightness = 1f
            }
        }

        onDispose {
            window.attributes = window.attributes.apply {
                screenBrightness = originalBrightness
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
private fun FlashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFFFFD54F),
            onPrimary = Color(0xFF1A1A1A),
            surface = Color(0xFF1D2028),
            onSurface = Color.White
        ),
        content = content
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun FlashScreenPreview() {
    FlashTheme {
        FlashScreenContent(
            uiState = FlashUiState(
                isFlashAvailable = true,
                hasCameraPermission = true
            ),
            onToggleFlash = {},
            onToggleScreenLight = {},
            onToggleSos = {},
            onSelectTimer = {},
            onCancelTimer = {},
            onRequestCameraPermission = {}
        )
    }
}
