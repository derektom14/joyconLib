package org.joyconLib

class JoyconTranslator(
        private val type: JoyconType,
        private val calculator: JoyconStickCalc
) {

    private var stickCalcX = IntArray(3)
    private var stickCalcY = IntArray(3)

    private var lastShared = 0
    private var lastSpecific = 0
    val inputs = mutableMapOf<String, Boolean>()
    private var oldInputs: Map<String, Boolean>? = null

    var horizontal = 0f
        private set
    var vertical = 0f
        private set
    var battery: Byte = 0
        private set

    fun calibrate(calibration: IntArray) {
        when (type) {
            JoyconType.LEFT -> {
                stickCalcX[1] = calibration[4] shl 8 and 0xF00 or calibration[3]
                stickCalcY[1] = calibration[5] shl 4 or (calibration[4] shr 4)
                stickCalcX[0] = stickCalcX[1] - (calibration[7] shl 8 and 0xF00 or calibration[6])
                stickCalcY[0] = stickCalcY[1] - (calibration[8] shl 4 or (calibration[7] shr 4))
                stickCalcX[2] = stickCalcX[1] + (calibration[1] shl 8 and 0xF00 or calibration[0])
                stickCalcY[2] = stickCalcY[1] + (calibration[2] shl 4 or (calibration[2] shr 4))
            }
            JoyconType.RIGHT -> {
                stickCalcX[1] = calibration[10] shl 8 and 0xF00 or calibration[9]
                stickCalcY[1] = calibration[11] shl 4 or (calibration[10] shr 4)
                stickCalcX[0] = stickCalcX[1] - (calibration[13] shl 8 and 0xF00 or calibration[12])
                stickCalcY[0] = stickCalcY[1] - (calibration[14] shl 4 or (calibration[13] shr 4))
                stickCalcX[2] = stickCalcX[1] + (calibration[16] shl 8 and 0xF00 or calibration[15])
                stickCalcY[2] = stickCalcY[1] + (calibration[17] shl 4 or (calibration[16] shr 4))
            }
        }
    }

    fun translate(dataBytes: ByteArray) {
        inputs.clear()

        val data = dataBytes.asSequence().map { it.unsigned() }.toList().toIntArray()
        val i = type.choose(5, 8)
        val x = data[i] or (data[i + 1] and 0xF shl 8)
        val y = data[i + 1] shr 4 or (data[i+2] shl 4)
        calculator.analogStickCalc(x, y, stickCalcX, stickCalcY)

        horizontal = calculator.horizontal
        vertical = calculator.vertical

        val shared = data[3]
        val specific = data[type.choose(4, 2)]
        val sharedByte = shared - lastShared
        lastShared = shared
        val specificByte = specific - lastSpecific
        lastSpecific = specific

        battery = dataBytes[1]

        //Inputs translation
        when (sharedByte) {
            JoyconConstant.PLUS_ON -> inputs.put(JoyconConstant.PLUS, true)
            JoyconConstant.PLUS_OFF -> inputs.put(JoyconConstant.PLUS, false)
            JoyconConstant.RIGHT_STICK_ON -> inputs.put(JoyconConstant.RIGHT_STICK, true)
            JoyconConstant.RIGHT_STICK_OFF -> inputs.put(JoyconConstant.RIGHT_STICK, false)
            JoyconConstant.HOME_ON -> inputs.put(JoyconConstant.HOME, true)
            JoyconConstant.HOME_OFF -> inputs.put(JoyconConstant.HOME, false)
        }
        when (type) {
            JoyconType.LEFT -> {
                when (specificByte) {
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
            }
            JoyconType.RIGHT -> {
                when (specificByte) {
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
            }
        }

        //Clearing inputs if the same
        if (inputs == oldInputs) {
            oldInputs = inputs
            inputs.clear()
        }
    }

}