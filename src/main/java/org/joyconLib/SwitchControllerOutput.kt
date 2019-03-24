package org.joyconLib

import java.awt.geom.Point2D

data class SwitchControllerOutput(
        val buttons: EnumBitset<SwitchButton>,
        val battery: Int,
        val leftStick: Point2D.Float,
        val rightStick: Point2D.Float
)