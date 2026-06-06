package kr.pe.dongha.flash2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val viewModel: FlashViewModel by viewModels()

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onCameraPermissionResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.setCameraPermission(
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )

        setContent {
            FlashScreen(
                viewModel = viewModel,
                onRequestCameraPermission = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopAll()
    }
}
