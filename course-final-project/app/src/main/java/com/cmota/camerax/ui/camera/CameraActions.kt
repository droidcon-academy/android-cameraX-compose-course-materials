package com.cmota.camerax.ui.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cmota.camerax.R
import com.cmota.camerax.ui.components.AddActionTextButton
import com.cmota.camerax.ui.components.CustomButton
import kotlinx.coroutines.CoroutineScope

@Composable
fun CameraMode(
    isCameraPhotoMode: MutableState<Boolean>
) {
    Spacer(modifier = Modifier.height(15.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        AddActionTextButton(
            text = R.string.action_photo,
            selected = isCameraPhotoMode.value,
            onClick = {
                isCameraPhotoMode.value = true
            }
        )

        Spacer(modifier = Modifier.width(30.dp))

        AddActionTextButton(
            text = R.string.action_video,
            selected = !isCameraPhotoMode.value,
            onClick = {
                isCameraPhotoMode.value = false
            }
        )
    }
}

@Composable
fun CameraActions(
    context: Context,
    recording: Boolean,
    imageCapture: ImageCapture,
    lensFacing: MutableState<Int>,
    outputPath: MutableState<Uri?>,
    mimeType: String,
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            horizontalArrangement = Arrangement.Center
        ) {

            val scope = rememberCoroutineScope()

            if (recording) {
                CameraActionTakePhotoSmall(
                    context = context,
                    scope = scope,
                    lensFacing = lensFacing,
                    imageCapture = imageCapture
                )
            } else {
                CameraActionPreview(
                    context = context,
                    mimeType = mimeType,
                    outputPath = outputPath)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            horizontalArrangement = Arrangement.Center
        ) {

            if (recording) {
                CameraActionRecordVideo(onClick = onClick)
            } else {
                CameraActionTakePhoto(onClick = onClick)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            horizontalArrangement = Arrangement.Center
        ) {

            CameraActionRotate(
                lensFacing = lensFacing
            )
        }
    }
}

@Composable
fun CameraActionPreview(
    context: Context,
    mimeType: String,
    outputPath: MutableState<Uri?>
) {
    CustomButton(
        width = 45.dp,
        height = 45.dp,
        backgroundColor = Color.DarkGray,
        shape = CircleShape,
        onClick = {
            if (outputPath.value == null) {
                return@CustomButton
            }

            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(outputPath.value, mimeType)
            context.startActivity(intent)
        },
        content = {
            if (outputPath.value == null) {
                return@CustomButton
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(outputPath.value)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.description_preview),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
fun CameraActionTakePhoto(
    onClick: () -> Unit
) {
    CustomButton(
        width = 75.dp,
        height = 75.dp,
        backgroundColor = Color.White,
        shape = CircleShape,
        onClick = { onClick() },
        content = { }
    )
}

@Composable
fun CameraActionTakePhotoSmall(
    context: Context,
    scope: CoroutineScope,
    lensFacing: MutableState<Int>,
    imageCapture: ImageCapture
) {
    CustomButton(
        width = 45.dp,
        height = 45.dp,
        backgroundColor = Color.DarkGray,
        shape = CircleShape,
        onClick = {
            takePicture(
                context = context,
                scope = scope,
                lensFacing = lensFacing.value,
                imageCapture = imageCapture
            )
        },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_camera_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentDescription = stringResource(id = R.string.description_photo)
            )
        }
    )
}

@Composable
fun CameraActionRecordVideo(
    onClick: () -> Unit
) {
    CustomButton(
        width = 75.dp,
        height = 75.dp,
        backgroundColor = Color.Red,
        shape = CircleShape,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )
            }
        }
    )
}

@Composable
fun CameraActionRotate(
    lensFacing: MutableState<Int>
) {
    CustomButton(
        width = 45.dp,
        height = 45.dp,
        backgroundColor = Color.DarkGray,
        shape = CircleShape,
        onClick = {
            if (lensFacing.value == CameraSelector.LENS_FACING_BACK) {
                lensFacing.value = CameraSelector.LENS_FACING_FRONT
            } else {
                lensFacing.value = CameraSelector.LENS_FACING_BACK
            }
        },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_camera_rotate),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentDescription = stringResource(id = R.string.description_rotate)
            )
        }
    )
}