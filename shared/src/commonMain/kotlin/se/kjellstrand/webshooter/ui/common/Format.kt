package se.kjellstrand.webshooter.ui.common

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * KMP-friendly replacement for `String.format("%.${decimals}f", value)`. Uses a
 * period as the decimal separator (the JVM versions used the platform locale,
 * which on Swedish devices rendered "3,1"). Chart label code already strips
 * trailing ".0" / ",0" suffixes so both work.
 */
fun formatDecimal(value: Double, decimals: Int): String {
    if (decimals <= 0) return value.roundToLong().toString()
    val factor = 10.0.pow(decimals).toLong()
    val rounded = (value * factor).roundToLong()
    val abs = rounded.absoluteValue
    val sign = if (rounded < 0) "-" else ""
    val intPart = abs / factor
    val fracPart = abs % factor
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return "$sign$intPart.$fracStr"
}

fun formatDecimal(value: Float, decimals: Int): String =
    formatDecimal(value.toDouble(), decimals)
