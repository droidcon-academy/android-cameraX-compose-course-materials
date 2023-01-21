package com.cmota.camerax.utils

import kotlin.time.Duration.Companion.seconds

private const val FORMAT_TIME = "%02d:%02d"
private const val SECS_IN_MINUTES = 60

fun getFormattedTime(time: Int): String {
    val seconds = time.seconds.inWholeSeconds % SECS_IN_MINUTES
    val minutes = time.seconds.inWholeMinutes

    return String.format(FORMAT_TIME, minutes, seconds)
}
