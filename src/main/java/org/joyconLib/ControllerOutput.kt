package org.joyconLib

import java.awt.geom.Point2D
import java.util.BitSet

data class ControllerOutput(
        val buttons: BitSet,
        val battery: Int,
        val leftStick: Point2D.Float,
        val rightStick: Point2D.Float
)