package org.joyconLib

/**
 * Converts a byte into a [0-255] integer
 */
fun Byte.unsigned(): Int = if (this < 0) this.toInt() + 256 else this.toInt()