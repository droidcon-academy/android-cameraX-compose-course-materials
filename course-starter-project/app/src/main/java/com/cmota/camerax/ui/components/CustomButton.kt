package com.cmota.camerax.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddActionTextButton(
    @StringRes text: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    CustomButton(
        width = 90.dp,
        height = 30.dp,
        backgroundColor = if(selected) Color.DarkGray else Color.Transparent,
        shape = RoundedCornerShape(35.dp),
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = stringResource(id = text),
                    color = Color.White,
                    fontSize = if(selected) 15.sp else 12.sp,
                    fontWeight = if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

@Composable
fun AddActionButton(
    @DrawableRes resource: Int,
    @StringRes description: Int,
    colorFilter: ColorFilter?,
    onClick: () -> Unit
) {
    CustomButton(
        width = 40.dp,
        height = 40.dp,
        backgroundColor = Color.Transparent,
        shape = CircleShape,
        onClick = { onClick() },
        content = {
            Image(
                painter = painterResource(id = resource),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentDescription = stringResource(id = description),
                colorFilter = colorFilter
            )
        }
    )
}

@Composable
fun CustomButton(
    width: Dp,
    height: Dp,
    backgroundColor: Color,
    shape: Shape,
    onClick: () -> Unit,
    content: @Composable ()-> Unit = {}
) {
    val selected = remember { mutableStateOf(false) }
    val scale = animateFloatAsState(if (selected.value) 0.75f else 1f)

    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .width(width)
            .height(height)
            .scale(scale.value)
            .pointerInput(selected.value) {
                awaitPointerEventScope {
                    selected.value = if (selected.value) {
                        waitForUpOrCancellation()
                        false
                    } else {
                        awaitFirstDown(false)
                        true
                    }
                }
            },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = null,
        shape = shape,
        content = { content() }
    )
}