package org.joyconLib

import io.reactivex.Observable

interface SwitchController {
    val output: Observable<SwitchControllerOutput>
    val type: SwitchControllerType
    fun setToHidMode()
    fun setToNormalInputMode()
    fun setLightsBlinking()
    fun setLightsOff()
    fun enableVibration()
    fun disableVibration()
    fun vibrate(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side = Side.values())
    fun endVibration(vararg sides: Side = Side.values())
    fun calibrate()
    fun setPlayerLight(i: Int)
}