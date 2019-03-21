/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

import java.util.HashMap

/**
 * **The translator for the right joycon**
 *
 *
 * This class will translate the raw value of the joycon
 *
 * @version 1.0
 * @author goupil
 */
class RightTranslator(private val calculator: JoyconStickCalc, private val stick_cal_x_r: IntArray, private val stick_cal_y_r: IntArray) {

    private var lastShared: Int = 0
    private var lastRight: Int = 0
    val inputs: HashMap<String, Boolean>
    private var oldInputs: HashMap<String, Boolean>? = null
    var horizontal: Float = 0.toFloat()
        private set
    var vertical: Float = 0.toFloat()
        private set
    var battery: Byte = 0
        private set

    init {
        lastShared = 0
        lastRight = 0
        battery = 0
        horizontal = 0f
        vertical = 0f
        inputs = HashMap()
    }

    fun translate(data: ByteArray) {
        //Clearing the inputs
        inputs.clear()

        val temp = IntArray(12)
        for (i in 5..11) {
            val b = data[i]
            if (b < 0) {
                temp[i] = b + 256
            } else {
                temp[i] = b.toInt()
            }
        }
        val x = temp[8] or (temp[9] and 0xF shl 8)
        val y = temp[9] shr 4 or (temp[10] shl 4)
        calculator.analogStickCalc(x, y, stick_cal_x_r, stick_cal_y_r)

        horizontal = calculator.horizontal
        vertical = calculator.vertical

        //Getting input change
        var shared = data[3].toInt()
        var right = data[2].toInt()
        if (data[3] < 0) {
            shared = data[3] + 256
        }
        if (data[2] < 0) {
            right = data[2] + 256
        }
        val sharedByte = shared - lastShared
        lastShared = shared
        val rightByte = right - lastRight
        lastRight = right

        //Battery translation
        var batteryInt = data[1].toInt()
        if (data[1] < 0) {
            batteryInt = data[1] + 256
        }
        battery = java.lang.Byte.parseByte(Integer.toHexString(batteryInt).substring(0, 1))

        //Inputs translation
        when (sharedByte) {
            JoyconConstant.PLUS_ON -> inputs.put(JoyconConstant.PLUS, true)
            JoyconConstant.PLUS_OFF -> inputs.put(JoyconConstant.PLUS, false)
            JoyconConstant.RIGHT_STICK_ON -> inputs.put(JoyconConstant.RIGHT_STICK, true)
            JoyconConstant.RIGHT_STICK_OFF -> inputs.put(JoyconConstant.RIGHT_STICK, false)
            JoyconConstant.HOME_ON -> inputs.put(JoyconConstant.HOME, true)
            JoyconConstant.HOME_OFF -> inputs.put(JoyconConstant.HOME, false)
        }
        when (rightByte) {
            JoyconConstant.Y_ON -> inputs.put(JoyconConstant.Y, true)
            JoyconConstant.Y_OFF -> inputs.put(JoyconConstant.Y, false)
            JoyconConstant.X_ON -> inputs.put(JoyconConstant.X, true)
            JoyconConstant.X_OFF -> inputs.put(JoyconConstant.X, false)
            JoyconConstant.B_ON -> inputs.put(JoyconConstant.B, true)
            JoyconConstant.B_OFF -> inputs.put(JoyconConstant.B, false)
            JoyconConstant.A_ON -> inputs.put(JoyconConstant.A, true)
            JoyconConstant.A_OFF -> inputs.put(JoyconConstant.A, false)
            JoyconConstant.SR_ON -> inputs.put(JoyconConstant.SR, true)
            JoyconConstant.SR_OFF -> inputs.put(JoyconConstant.SR, false)
            JoyconConstant.SL_ON -> inputs.put(JoyconConstant.SL, true)
            JoyconConstant.SL_OFF -> inputs.put(JoyconConstant.SL, false)
            JoyconConstant.R_ON -> inputs.put(JoyconConstant.R, true)
            JoyconConstant.R_OFF -> inputs.put(JoyconConstant.R, false)
            JoyconConstant.ZR_ON -> inputs.put(JoyconConstant.ZR, true)
            JoyconConstant.ZR_OFF -> inputs.put(JoyconConstant.ZR, false)
        }
        //Clearing inputs if the same
        if (inputs == oldInputs) {
            oldInputs = inputs
            inputs.clear()
        }
    }

}
