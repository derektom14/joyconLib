/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

import java.awt.geom.Point2D

/**
 *
 * @author Administrateur
 */
class JoyconStickCalc {

    fun analogStickCalc(x: Int, y: Int, x_calc: IntArray, y_calc: IntArray): Point2D.Float {
        var x = x
        var y = y
        val xF: Float
        val yF: Float
        val hori: Float
        val vert: Float
        val deadZoneCenter = 0.15f
        val deadZoneOuter = 0.10f

        x = Math.max(x_calc[0], Math.min(x_calc[2], x))
        y = Math.max(y_calc[0], Math.min(y_calc[2], y))

        if (x >= x_calc[1]) {
            xF = (x - x_calc[1]).toFloat() / (x_calc[2] - x_calc[1]).toFloat()
        } else {
            xF = -((x - x_calc[1]).toFloat() / (x_calc[0] - x_calc[1]).toFloat())
        }
        if (y >= y_calc[1]) {
            yF = (y - y_calc[1]).toFloat() / (y_calc[2] - y_calc[1]).toFloat()
        } else {
            yF = -((y - y_calc[1]).toFloat() / (y_calc[0] - y_calc[1]).toFloat())
        }

        val mag = Math.sqrt((xF * xF + yF * yF).toDouble()).toFloat()

        if (mag > deadZoneCenter) {
            // scale such that output magnitude is in the range [0.0f, 1.0f]
            val legalRange = 1.0f - deadZoneOuter - deadZoneCenter
            val normalizedMag = Math.min(1.0f, (mag - deadZoneCenter) / legalRange)
            val scale = normalizedMag / mag
            hori = xF * scale
            vert = yF * scale
        } else {
            // stick is in the inner dead zone
            hori = 0.0f
            vert = 0.0f
        }

        return Point2D.Float(hori, vert)
    }

}
