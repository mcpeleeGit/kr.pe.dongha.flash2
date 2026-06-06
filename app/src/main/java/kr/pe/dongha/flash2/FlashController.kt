package kr.pe.dongha.flash2

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

class FlashController(context: Context) {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cameraId: String? = findFlashCameraId()
    private var torchCallback: CameraManager.TorchCallback? = null

    val isFlashAvailable: Boolean = cameraId != null

    fun registerTorchCallback(
        onChanged: (Boolean) -> Unit,
        onUnavailable: () -> Unit
    ) {
        if (torchCallback != null) return

        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(id: String, enabled: Boolean) {
                if (id == cameraId) onChanged(enabled)
            }

            override fun onTorchModeUnavailable(id: String) {
                if (id == cameraId) onUnavailable()
            }
        }

        torchCallback = callback
        cameraManager.registerTorchCallback(callback, mainHandler)
    }

    fun unregisterTorchCallback() {
        torchCallback?.let(cameraManager::unregisterTorchCallback)
        torchCallback = null
    }

    fun setFlash(enabled: Boolean): FlashResult {
        val id = cameraId ?: return FlashResult.NotSupported

        return try {
            cameraManager.setTorchMode(id, enabled)
            FlashResult.Success
        } catch (exception: SecurityException) {
            FlashResult.PermissionDenied
        } catch (exception: CameraAccessException) {
            FlashResult.Unavailable
        } catch (exception: IllegalArgumentException) {
            FlashResult.NotSupported
        } catch (exception: RuntimeException) {
            FlashResult.Unavailable
        }
    }

    private fun findFlashCameraId(): String? {
        return try {
            val cameraIds = cameraManager.cameraIdList
            val backCamera = cameraIds.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.hasFlash() &&
                    characteristics.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            }

            backCamera ?: cameraIds.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).hasFlash()
            }
        } catch (exception: CameraAccessException) {
            null
        } catch (exception: RuntimeException) {
            null
        }
    }

    private fun CameraCharacteristics.hasFlash(): Boolean {
        return get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }
}

sealed interface FlashResult {
    data object Success : FlashResult
    data object NotSupported : FlashResult
    data object PermissionDenied : FlashResult
    data object Unavailable : FlashResult
}
