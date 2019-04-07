package com.derekpeirce.switchcontroller.util

import java.awt.geom.Point2D

fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Byte.unsigned(): Int = when {
    this < 0 -> this + 256
    else -> this.toInt()
}

@ExperimentalUnsignedTypes
fun UShort.lowerByte() = this.toUByte()
@ExperimentalUnsignedTypes
fun UShort.upperByte() = (this / 256u).toUByte()

fun Point2D.Float.rotateLeft90() = Point2D.Float(-y, x)
fun Point2D.Float.rotateRight90() = Point2D.Float(y, -x)