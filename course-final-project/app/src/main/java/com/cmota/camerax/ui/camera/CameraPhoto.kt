package com.cmota.camerax.ui.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionMode
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.cmota.camerax.R
import com.cmota.camerax.extensions.getCameraExtensions
import com.cmota.camerax.extensions.getCameraProvider
import com.cmota.camerax.ui.components.AddActionButton
import com.cmota.camerax.utils.MIME_TYPE_IMAGE
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "CameraPhoto"

private lateinit var camera: Camera
private lateinit var outputPath: MutableState<Uri?>


@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraPhoto(
    isCameraPhotoMode: MutableState<Boolean>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    outputPath = remember { mutableStateOf(null) }

    val flash = remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    val lensFacing = remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val previewView = remember { PreviewView(context) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    imageCapture.flashMode = flash.value

    val isBokehAvailable = remember { mutableStateOf(false) }
    val isHdrAvailable = remember { mutableStateOf(false) }
    val isNightAvailable = remember { mutableStateOf(false) }
    val isFaceRetouchAvailable = remember { mutableStateOf(false) }
    val isAutoAvailable = remember { mutableStateOf(false) }

    val extensionSelected = remember { mutableStateOf(ExtensionMode.NONE) }

    LaunchedEffect(key1 = lensFacing.value, key2 = extensionSelected.value) {
        val cameraProvider = context.getCameraProvider()
        val extensionsManager = context.getCameraExtensions(cameraProvider)

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing.value)
            .build()

        cameraProvider.unbindAll()

        val selector = extensionsManager.getExtensionEnabledCameraSelector(
            cameraSelector,
            extensionSelected.value
        )

        camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)

        preview.setSurfaceProvider(previewView.surfaceProvider)

        val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale: Float = (camera.cameraInfo.zoomState.value?.zoomRatio ?: 0f).times(
                    detector.scaleFactor
                )
                camera.cameraControl.setZoomRatio(scale)
                return true
            }
        })

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }

        isBokehAvailable.value = extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.BOKEH)
        isHdrAvailable.value = extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.HDR)
        isNightAvailable.value = extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.NIGHT)
        isFaceRetouchAvailable.value = extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.FACE_RETOUCH)
        isAutoAvailable.value = extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.AUTO)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.10f)
                .background(Color.Black),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            CameraExtensions(
                flash = flash,
                extensionSelected = extensionSelected,
                isBokehAvailable = isBokehAvailable,
                isHdrAvailable = isHdrAvailable,
                isNightAvailable = isNightAvailable,
                isFaceRetouchAvailable = isFaceRetouchAvailable,
                isAutoAvailable = isAutoAvailable
            )
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

            val scope = rememberCoroutineScope()

            CameraActions(
                context = context,
                recording = false,
                imageCapture = imageCapture,
                lensFacing = lensFacing,
                outputPath = outputPath,
                mimeType = MIME_TYPE_IMAGE,
                onClick = {
                    takePicture(
                        context = context,
                        scope = scope,
                        lensFacing = lensFacing.value,
                        imageCapture = imageCapture
                    )
                }
            )
        }
    }
}

@Composable
fun CameraExtensions(
    flash: MutableState<Int>,
    extensionSelected: MutableState<Int>,
    isBokehAvailable: MutableState<Boolean>,
    isHdrAvailable: MutableState<Boolean>,
    isNightAvailable: MutableState<Boolean>,
    isFaceRetouchAvailable: MutableState<Boolean>,
    isAutoAvailable: MutableState<Boolean>
) {
    AddActionButton(
        resource = if (flash.value == ImageCapture.FLASH_MODE_OFF) {
            R.drawable.ic_camera_flash_off
        } else {
            R.drawable.ic_camera_flash_on
        },
        description = R.string.description_rotate,
        onClick = {
            if (flash.value == ImageCapture.FLASH_MODE_OFF) {
                flash.value = ImageCapture.FLASH_MODE_ON
            } else {
                flash.value = ImageCapture.FLASH_MODE_OFF
            }
        },
        colorFilter = null
    )

    if (isHdrAvailable.value) {
        AddActionButton(
            resource = R.drawable.ic_camera_hdr,
            description = R.string.description_hdr,
            onClick = {
                if (extensionSelected.value == ExtensionMode.HDR) {
                    extensionSelected.value = ExtensionMode.NONE
                } else {
                    extensionSelected.value = ExtensionMode.HDR
                }
            },
            colorFilter = if(extensionSelected.value == ExtensionMode.HDR) {
                ColorFilter.tint(Color.Yellow)
            } else {
                null
            }
        )
    }

    if (isBokehAvailable.value) {
        AddActionButton(
            resource = R.drawable.ic_camera_bokeh,
            description = R.string.description_bokeh,
            onClick = {
                if (extensionSelected.value == ExtensionMode.BOKEH) {
                    extensionSelected.value = ExtensionMode.NONE
                } else {
                    extensionSelected.value = ExtensionMode.BOKEH
                }
            },
            colorFilter = if(extensionSelected.value == ExtensionMode.BOKEH) {
                ColorFilter.tint(Color.Yellow)
            } else {
                null
            }
        )
    }

    if (isNightAvailable.value) {
        AddActionButton(
            resource = R.drawable.ic_camera_night,
            description = R.string.description_night,
            onClick = {
                if (extensionSelected.value == ExtensionMode.NIGHT) {
                    extensionSelected.value = ExtensionMode.NONE
                } else {
                    extensionSelected.value = ExtensionMode.NIGHT
                }
            },
            colorFilter = if(extensionSelected.value == ExtensionMode.NIGHT) {
                ColorFilter.tint(Color.Yellow)
            } else {
                null
            }
        )
    }

    if (isFaceRetouchAvailable.value) {
        AddActionButton(
            resource = R.drawable.ic_camera_face_retouch,
            description = R.string.description_face_retouch,
            onClick = {
                if (extensionSelected.value == ExtensionMode.FACE_RETOUCH) {
                    extensionSelected.value = ExtensionMode.NONE
                } else {
                    extensionSelected.value = ExtensionMode.FACE_RETOUCH
                }
            },
            colorFilter = if(extensionSelected.value == ExtensionMode.FACE_RETOUCH) {
                ColorFilter.tint(Color.Yellow)
            } else {
                null
            }
        )
    }

    if (isAutoAvailable.value) {
        AddActionButton(
            resource = R.drawable.ic_camera_auto,
            description = R.string.description_auto,
            onClick = {
                if (extensionSelected.value == ExtensionMode.AUTO) {
                    extensionSelected.value = ExtensionMode.NONE
                } else {
                    extensionSelected.value = ExtensionMode.AUTO
                }
            },
            colorFilter = if(extensionSelected.value == ExtensionMode.AUTO) {
                ColorFilter.tint(Color.Yellow)
            } else {
                null
            }
        )
    }
}

fun takePicture(
    context: Context,
    scope: CoroutineScope,
    lensFacing: Int,
    imageCapture: ImageCapture
) {

    val name = "${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
    }

    val metadata = ImageCapture.Metadata()
    metadata.isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT

    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
        .setMetadata(metadata)
        .build()
    imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(error: ImageCaptureException) {
                Log.e(TAG, "Unable save image. Error:$error")
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.d(TAG, "Image saved successfully at: ${outputFileResults.savedUri}")
                scope.launch {
                    Toast
                        .makeText(context, R.string.info_image_saved, Toast.LENGTH_SHORT)
                        .show()

                    outputPath.value = outputFileResults.savedUri
                }
            }
        })
}