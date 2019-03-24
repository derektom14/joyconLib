package org.joyconLib

import io.reactivex.Completable
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

    override fun enableVibration() {
        left.enableVibration()
        right.enableVibration()
    }

    override fun disableVibration() {
        left.disableVibration()
        right.disableVibration()
    }

    override fun vibrate(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side) {
        left.vibrate(frequencies, amplitude, *sides)
        right.vibrate(frequencies, amplitude, *sides)
//        if (sides.contains(Side.LEFT)) {
//            left.vibrate(frequencies, amplitude, Side.LEFT)
//        }
//        if (sides.contains(Side.RIGHT)) {
//            right.vibrate(frequencies, amplitude, Side.RIGHT)
//        }
    }

    override fun endVibration(vararg sides: Side) {
        if (sides.contains(Side.LEFT)) {
            left.endVibration(Side.LEFT)
        }
        if (sides.contains(Side.RIGHT)) {
            right.endVibration(Side.RIGHT)
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