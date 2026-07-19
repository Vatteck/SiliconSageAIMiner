package com.hashfactory.game.ui.format

import java.util.Locale
import kotlin.math.abs

private val SUFFIXES = listOf("", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc")

/** 1234.5 -> "1.23K"; whole numbers below 1000 render without decimals. */
fun formatFlops(value: Double): String {
    val v = abs(value)
    if (v < 1000.0) {
        return if (v == v.toLong().toDouble()) value.toLong().toString()
        else String.format(Locale.US, "%.1f", value)
    }
    var scaled = v
    var idx = 0
    while (scaled >= 1000.0 && idx < SUFFIXES.lastIndex) {
        scaled /= 1000.0
        idx++
    }
    if (scaled >= 1000.0) return String.format(Locale.US, "%.2e", value)
    val sign = if (value < 0) "-" else ""
    return sign + String.format(Locale.US, "%.2f%s", scaled, SUFFIXES[idx])
}

fun formatDuration(totalSeconds: Double): String {
    val s = totalSeconds.toLong().coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${sec}s"
        else -> "${sec}s"
    }
}
