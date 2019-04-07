package com.derekpeirce.switchcontroller

import io.reactivex.Observable

/**
 * Represents any Switch controller
 */
interface SwitchController {
    /**
     * The controller's outputs, produced roughly every 16ms.
     */
    val output: Observable<SwitchControllerOutput>
    /**
     * The controller's outputs, but a single Joycon has its buttons and Joysticks mapped as if it were held
     * horizontally as a Pro controller.
     */
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