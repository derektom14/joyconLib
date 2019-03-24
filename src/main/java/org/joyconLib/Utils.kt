package org.joyconLib

import java.awt.geom.Point2D

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

fun Point2D.Float.rotateLeft90() = Point2D.Float(-y, x)
fun Point2D.Float.rotateRight90() = Point2D.Float(-x, y)