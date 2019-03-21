/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

import java.util.HashMap

/**
 * **The translator for the left joycon**
 *
 *
 * This class will translate the raw value of the joycon
 *
 * @version 1.0
 * @author goupil
 */
class LeftTranslator(private val calculator: JoyconStickCalc, private val stick_cal_x_l: IntArray, private val stick_cal_y_l: IntArray) {

    private var lastShared: Int = 0
    private var lastLeft: Int = 0
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
        lastLeft = 0
        battery = 0
        horizontal = 0f
        vertical = 0f
        inputs = HashMap()
    }

    fun translate(data: ByteArray) {
        //Clearing the inputs
        inputs.clear()

        val temp = IntArray(8)
        for (i in 5..7) {
            temp[i] = data[i].unsigned()
        }
        val x = temp[5] or (temp[6] and 0xF shl 8)
        val y = temp[6] shr 4 or (temp[7] shl 4)
        calculator.analogStickCalc(x, y, stick_cal_x_l, stick_cal_y_l)

        horizontal = calculator.horizontal
        vertical = calculator.vertical

        //Getting input change
        val shared = data[3].unsigned()
        val left = data[4].unsigned()
        val sharedByte = shared - lastShared
        lastShared = shared
        val leftByte = left - lastLeft
        lastLeft = left

        //Battery translation
        val batteryInt = data[1].unsigned()
        battery = java.lang.Byte.parseByte(Integer.toHexString(batteryInt).substring(0, 1))

        //Inputs translation
        when (sharedByte) {
            JoyconConstant.MINUS_ON -> inputs.put(JoyconConstant.MINUS, true)
            JoyconConstant.MINUS_OFF -> inputs.put(JoyconConstant.MINUS, false)
            JoyconConstant.LEFT_STICK_ON -> inputs.put(JoyconConstant.LEFT_STICK, true)
            JoyconConstant.LEFT_STICK_OFF -> inputs.put(JoyconConstant.LEFT_STICK, false)
            JoyconConstant.CAPTURE_ON -> inputs.put(JoyconConstant.CAPTURE, true)
            JoyconConstant.CAPTURE_OFF -> inputs.put(JoyconConstant.CAPTURE, false)
        }
        when (leftByte) {
            JoyconConstant.DOWN_ON -> inputs.put(JoyconConstant.DOWN, true)
            JoyconConstant.DOWN_OFF -> inputs.put(JoyconConstant.DOWN, false)
            JoyconConstant.UP_ON -> inputs.put(JoyconConstant.UP, true)
            JoyconConstant.UP_OFF -> inputs.put(JoyconConstant.UP, false)
            JoyconConstant.RIGHT_ON -> inputs.put(JoyconConstant.RIGHT, true)
            JoyconConstant.RIGHT_OFF -> inputs.put(JoyconConstant.RIGHT, false)
            JoyconConstant.LEFT_ON -> inputs.put(JoyconConstant.LEFT, true)
            JoyconConstant.LEFT_OFF -> inputs.put(JoyconConstant.LEFT, false)
            JoyconConstant.SR_ON -> inputs.put(JoyconConstant.SR, true)
            JoyconConstant.SR_OFF -> inputs.put(JoyconConstant.SR, false)
            JoyconConstant.SL_ON -> inputs.put(JoyconConstant.SL, true)
            JoyconConstant.SL_OFF -> inputs.put(JoyconConstant.SL, false)
            JoyconConstant.L_ON -> inputs.put(JoyconConstant.L, true)
            JoyconConstant.L_OFF -> inputs.put(JoyconConstant.L, false)
            JoyconConstant.ZL_ON -> inputs.put(JoyconConstant.ZL, true)
            JoyconConstant.ZL_OFF -> inputs.put(JoyconConstant.ZL, false)
        }
        //Clearing inputs if the same
        if (inputs == oldInputs) {
            oldInputs = inputs
            inputs.clear()
        }
    }

}
