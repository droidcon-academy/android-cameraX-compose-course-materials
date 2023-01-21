package com.cmota.camerax.ui.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmota.camerax.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissionsIfNeeded(
    cancelAction: () -> Unit,
) {
    val allPermissionState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
    )
    if (!allPermissionState.allPermissionsGranted) {
        if (allPermissionState.revokedPermissions.isNotEmpty()) {
            if (allPermissionState.revokedPermissions[0].status.shouldShowRationale) {
                val message = when (allPermissionState.revokedPermissions[0].permission) {
                    Manifest.permission.CAMERA -> {
                        R.string.permission_missing_camera
                    }
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        R.string.permission_missing_storage
                    }
                    else -> {
                        R.string.permission_missing_audio
                    }
                }

                ShowPermissionRationaleDialog(
                    cancelAction = cancelAction,
                    message = message
                )
            } else {
                LaunchedEffect(key1 = !allPermissionState.allPermissionsGranted) {
                    launch {
                        allPermissionState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }
}

@Composable
fun ShowPermissionRationaleDialog(
    cancelAction: () -> Unit,
    @StringRes message: Int,
) {

    AlertDialog(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colors.onSurface, RoundedCornerShape(8.dp)),
        onDismissRequest = {
            cancelAction()
        },
        text = {
            Text(
                text = stringResource(id = message),
                color = Color.White,
                fontSize = 17.sp
            )
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            cancelAction()
                        }) {
                        Text(
                            text = stringResource(id = R.string.permission_missing_cancel),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val context = LocalContext.current

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )

                            context.startActivity(intent)
                        }) {
                        Text(
                            text = stringResource(id = R.string.permission_missing_settings),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}