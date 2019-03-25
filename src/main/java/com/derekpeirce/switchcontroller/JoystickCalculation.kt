/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.derekpeirce.switchcontroller

import com.derekpeirce.switchcontroller.util.clamp
import java.awt.geom.Point2D

/**
 *
 * @author Administrateur
 */

fun analogStickCalc(xIn: Int, yIn: Int, xCalibration: IntArray, yCalibration: IntArray): Point2D.Float {
    val deadZoneCenter = 0.15f
    val deadZoneOuter = 0.10f

    val x = xIn.clamp(xCalibration[0], xCalibration[2])
    val y = yIn.clamp(yCalibration[0], yCalibration[2])

    val xF = if (x >= xCalibration[1]) {
        (x - xCalibration[1]).toFloat() / (xCalibration[2] - xCalibration[1]).toFloat()
    } else {
        -((x - xCalibration[1]).toFloat() / (xCalibration[0] - xCalibration[1]).toFloat())
    }
    val yF = if (y >= yCalibration[1]) {
        (y - yCalibration[1]).toFloat() / (yCalibration[2] - yCalibration[1]).toFloat()
    } else {
        -((y - yCalibration[1]).toFloat() / (yCalibration[0] - yCalibration[1]).toFloat())
    }

    val mag = Math.sqrt((xF * xF + yF * yF).toDouble()).toFloat()

    return if (mag > deadZoneCenter) {
        // scale such that output magnitude is in the range [0.0f, 1.0f]
        val legalRange = 1.0f - deadZoneOuter - deadZoneCenter
        val normalizedMag = Math.min(1.0f, (mag - deadZoneCenter) / legalRange)
        val scale = normalizedMag / mag
        Point2D.Float(xF * scale, yF * scale)
    } else {
        // stick is in the inner dead zone
        Point2D.Float(0f, 0f)
    }
}
