package org.joyconLib

fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Double.roundToInt() = Math.round(this).toInt()

fun UShort.lowerByte() = this.toUByte()
fun UShort.upperByte() = (this / 256u).toUByte()