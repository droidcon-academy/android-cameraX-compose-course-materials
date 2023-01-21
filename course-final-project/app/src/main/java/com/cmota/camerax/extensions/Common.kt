package com.cmota.camerax.extensions

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraProvider
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "Common"


suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine {
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            it.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

suspend fun Context.getCameraExtensions(cameraProvider: CameraProvider): ExtensionsManager = suspendCoroutine {
    ExtensionsManager.getInstanceAsync(this, cameraProvider).also { extensionManager ->
        it.resume(extensionManager.get())
    }
}

fun Quality.qualityToString() : String {
    return when (this) {
        Quality.UHD -> "UHD"
        Quality.FHD -> "FHD"
        Quality.HD -> "HD"
        Quality.SD -> "SD"
        else -> {
            Log.e(TAG, "Unknown parameter: $this")
            throw IllegalStateException()
        }
    }
}