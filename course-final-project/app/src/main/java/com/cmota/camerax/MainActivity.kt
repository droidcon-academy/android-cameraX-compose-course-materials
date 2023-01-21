package com.cmota.camerax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.cmota.camerax.ui.camera.CameraPhoto
import com.cmota.camerax.ui.camera.CameraVideo
import com.cmota.camerax.ui.permissions.RequestPermissionsIfNeeded
import com.cmota.camerax.ui.theme.CameraXTheme
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val cancelAction = { finish() }

        setContent {
            CameraXTheme {
                val systemUiController: SystemUiController = rememberSystemUiController()
                systemUiController.isStatusBarVisible = false

                val isCameraPhotoMode = remember { mutableStateOf(true) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {

                    RequestPermissionsIfNeeded(
                        cancelAction = cancelAction
                    )

                    if (isCameraPhotoMode.value) {
                        CameraPhoto(
                            isCameraPhotoMode = isCameraPhotoMode
                        )
                    } else {
                        CameraVideo(
                            isCameraPhotoMode = isCameraPhotoMode
                        )
                    }
                }
            }
        }
    }
}