package com.derekpeirce.switchcontroller

import io.reactivex.Observable
import io.reactivex.functions.BiFunction

data class JoyconPair(
        private val left: SwitchController,
        private val right: SwitchController
) : SwitchController {

    init {
        require(left.type == SwitchControllerType.LEFT_JOYCON && right.type == SwitchControllerType.RIGHT_JOYCON) {
            "Paired ${left.type} and ${right.type}"
        }
    }

    override val type: SwitchControllerType
        get() = SwitchControllerType.PAIRED_JOYCONS

    override val output: Observable<SwitchControllerOutput>
        get() = Observable.combineLatest(left.output, right.output, BiFunction { leftOutput, rightOutput ->
            SwitchControllerOutput(
                    leftOutput.buttons + rightOutput.buttons,
                    leftOutput.battery + rightOutput.battery,
                    leftOutput.leftStick,
                    rightOutput.rightStick
            )
        })

    override val horizontalOutput: Observable<SwitchControllerOutput>
        get() = output

    override fun setToHidMode() {
        left.setToHidMode()
        right.setToHidMode()
    }

    override fun setToNormalInputMode() {
        left.setToNormalInputMode()
        right.setToNormalInputMode()
    }

    override fun setLightsBlinking() {
        left.setLightsBlinking()
        right.setLightsBlinking()
    }

    override fun setLightsOff() {
        left.setLightsOff()
        right.setLightsOff()
    }

    override fun enableRumble() {
        left.enableRumble()
        right.enableRumble()
    }

    override fun disableRumble() {
        left.disableRumble()
        right.disableRumble()
    }

    override fun rumble(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side) {
        left.rumble(frequencies, amplitude, *sides)
        right.rumble(frequencies, amplitude, *sides)
//        if (sides.contains(Side.LEFT)) {
//            left.rumble(frequencies, amplitude, Side.LEFT)
//        }
//        if (sides.contains(Side.RIGHT)) {
//            right.rumble(frequencies, amplitude, Side.RIGHT)
//        }
    }

    override fun endRumble(vararg sides: Side) {
        if (sides.contains(Side.LEFT)) {
            left.endRumble(Side.LEFT)
        }
        if (sides.contains(Side.RIGHT)) {
            right.endRumble(Side.RIGHT)
        }
    }

    override fun calibrate() {
        left.calibrate()
        right.calibrate()
    }

    override fun setPlayerLight(i: Int) {
        left.setPlayerLight(i)
        right.setPlayerLight(i)
    }

}