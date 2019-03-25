package com.derekpeirce.switchcontroller

import io.reactivex.Completable
import io.reactivex.Observable

interface SwitchController {
    val output: Observable<SwitchControllerOutput>
    val horizontalOutput: Observable<SwitchControllerOutput>
    val type: SwitchControllerType
    fun setToHidMode()
    fun setToNormalInputMode()
    fun setLightsBlinking()
    fun setLightsOff()
    fun enableRumble()
    fun disableRumble()
    fun rumble(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side = Side.values())
    fun endRumble(vararg sides: Side = Side.values())
    fun calibrate()
    fun setPlayerLight(i: Int)
}