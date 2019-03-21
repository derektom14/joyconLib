package org.joyconLib

enum class JoyconType {
    LEFT,
    RIGHT,
    ;

    fun <T> choose(left: T, right: T) = when(this) {
        JoyconType.LEFT -> left
        JoyconType.RIGHT -> right
    }
}