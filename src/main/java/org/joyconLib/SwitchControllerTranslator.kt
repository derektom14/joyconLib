package org.joyconLib

import java.awt.geom.Point2D
import java.util.*

class SwitchControllerTranslator(
        private val calculator: JoyconStickCalc
) {

    private var stickCalcXL = IntArray(3)
    private var stickCalcYL = IntArray(3)

    private var stickCalcXR = IntArray(3)
    private var stickCalcYR = IntArray(3)

    fun calibrate(calibration: IntArray) {
        stickCalcXL[1] = calibration[4] shl 8 and 0xF00 or calibration[3]
        stickCalcYL[1] = calibration[5] shl 4 or (calibration[4] shr 4)
        stickCalcXL[0] = stickCalcXL[1] - (calibration[7] shl 8 and 0xF00 or calibration[6])
        stickCalcYL[0] = stickCalcYL[1] - (calibration[8] shl 4 or (calibration[7] shr 4))
        stickCalcXL[2] = stickCalcXL[1] + (calibration[1] shl 8 and 0xF00 or calibration[0])
        stickCalcYL[2] = stickCalcYL[1] + (calibration[2] shl 4 or (calibration[2] shr 4))
        stickCalcXR[1] = calibration[10] shl 8 and 0xF00 or calibration[9]
        stickCalcYR[1] = calibration[11] shl 4 or (calibration[10] shr 4)
        stickCalcXR[0] = stickCalcXR[1] - (calibration[13] shl 8 and 0xF00 or calibration[12])
        stickCalcYR[0] = stickCalcYR[1] - (calibration[14] shl 4 or (calibration[13] shr 4))
        stickCalcXR[2] = stickCalcXR[1] + (calibration[16] shl 8 and 0xF00 or calibration[15])
        stickCalcYR[2] = stickCalcYR[1] + (calibration[17] shl 4 or (calibration[16] shr 4))
    }

    private fun translateStick(data: List<Int>, stickCalcX: IntArray, stickCalcY: IntArray): Point2D.Float {
        val x = data[0] or (data[1] and 0xF shl 8)
        val y = data[1] shr 4 or (data[2] shl 4)
        return calculator.analogStickCalc(x, y, stickCalcX, stickCalcY)
    }

    fun translate(dataBytes: ByteArray): SwitchControllerOutput {

        val leftStick = translateStick(dataBytes.sliceArray(5..7).map { it.toUByte().toInt() }, stickCalcXL, stickCalcYL)
        val rightStick = translateStick(dataBytes.sliceArray(8..10).map{ it.toUByte().toInt() }, stickCalcXR, stickCalcYR)

        val bitSet = BitSet.valueOf(dataBytes.slice(2..4).toByteArray())
        bitSet.clear(SwitchButton.UNKNOWN_1.ordinal)
        bitSet.clear(SwitchButton.UNKNOWN_2.ordinal)

        return SwitchControllerOutput(
                EnumBitset(bitSet, SwitchButton.values()),
                battery = listOf(dataBytes[1].toInt()),
                leftStick = leftStick,
                rightStick = rightStick
        )
    }

}