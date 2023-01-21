package com.cmota.camerax.ui.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cmota.camerax.R
import com.cmota.camerax.extensions.getCameraProvider
import com.cmota.camerax.ui.components.AddActionButton
import com.cmota.camerax.ui.permissions.hasRecordAudioPermission
import com.cmota.camerax.utils.MIME_TYPE_VIDEO
import com.cmota.camerax.utils.getFormattedTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private const val TAG = "CameraVideo"

private lateinit var camera: Camera
private lateinit var outputPath: MutableState<Uri?>

private var recording: Recording? = null


@Composable
fun CameraVideo(
    isCameraPhotoMode: MutableState<Boolean>
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    outputPath = remember { mutableStateOf(null) }

    val flash = remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    val lensFacing = remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val previewView = remember { PreviewView(context) }

    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))

    val recorder = Recorder.Builder()
        .setQualitySelector(qualitySelector)
        .build()

    val videoCapture = VideoCapture.withOutput(recorder)

    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(key1 = lensFacing.value) {
        val cameraProvider = context.getCameraProvider()

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing.value)
            .build()

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture, imageCapture)

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Column {

        val secs = remember { mutableStateOf(0) }
        val showTimer = remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.weight(0.10f),
            color = Color.Black
        ) {

            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                AddActionButton(
                    resource = if (flash.value == ImageCapture.FLASH_MODE_OFF) {
                        R.drawable.ic_camera_flash_off
                    } else {
                        R.drawable.ic_camera_flash_on
                    },
                    description = R.string.description_flash,
                    onClick = {
                        if (flash.value == ImageCapture.FLASH_MODE_OFF) {
                            flash.value = ImageCapture.FLASH_MODE_ON
                        } else {
                            flash.value = ImageCapture.FLASH_MODE_OFF
                        }

                        camera.cameraControl.enableTorch(flash.value == ImageCapture.FLASH_MODE_ON)
                    },
                    colorFilter = null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                LaunchedEffect(key1 = showTimer.value) {
                    while (showTimer.value) {
                        delay(1.seconds)
                        secs.value += 1
                    }
                }

                if (showTimer.value) {
                    Text(
                        text = getFormattedTime(secs.value),
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }
            }
        }

        //Required by: https://issuetracker.google.com/issues/242463987
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1.0f)
        ) {
            AndroidView(
                factory = {
                    previewView.also {
                        it.clipToOutline = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(Color.Black),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            CameraMode(
                isCameraPhotoMode = isCameraPhotoMode
            )

            CameraActions(
                context = context,
                recording = showTimer.value,
                imageCapture = imageCapture,
                lensFacing = lensFacing,
                outputPath = outputPath,
                mimeType = MIME_TYPE_VIDEO,
                onClick = {
                    if (recording == null) {
                        secs.value = 0
                        startRecording(
                            context = context,
                            scope = scope,
                            videoCapture = videoCapture
                        )
                    } else {
                        stopRecording()
                    }

                    showTimer.value = recording != null
                }
            )
        }
    }
}

@SuppressLint("MissingPermission")
fun startRecording(
    context: Context,
    scope: CoroutineScope,
    videoCapture: VideoCapture<Recorder>,
) {
    val name = "${System.currentTimeMillis()}.mp4"
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, name)
    }

    val mediaStoreOutput = MediaStoreOutputOptions
        .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        .setContentValues(contentValues)
        .build()

    val prepareRecording = videoCapture.output
        .prepareRecording(context, mediaStoreOutput)

    if (hasRecordAudioPermission(context)) {
        prepareRecording.withAudioEnabled()
    }

    recording = prepareRecording.start(ContextCompat.getMainExecutor(context)) { event ->
        when (event) {
            is VideoRecordEvent.Start -> {
                Log.d(TAG, "Recording started")
            }

            is VideoRecordEvent.Finalize -> {
                Log.d(TAG, "Video saved successfully at: ${event.outputResults.outputUri}")
                scope.launch {
                    Toast
                        .makeText(context, R.string.info_video_saved, Toast.LENGTH_SHORT)
                        .show()
                }

                outputPath.value = event.outputResults.outputUri
            }

            else -> {
                Log.d(TAG, "Recording state: $event")
            }
        }
    }

    Log.d(TAG, "Starting recording: $recording")
}

fun stopRecording() {
    recording?.stop()
    recording = null
}