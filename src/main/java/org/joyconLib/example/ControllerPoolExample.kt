package org.joyconLib.example

import org.joyconLib.SwitchButton
import org.joyconLib.SwitchControllerOutput
import org.joyconLib.SwitchButton.*
import org.joyconLib.pollDevices

fun main(args: Array<String>) {
    val controllers = pollDevices()
    println(controllers)

    controllers.forEach { controller ->
        controller.setToHidMode()
        controller.setLightsBlinking()
        controller.enableVibration()
        controller.startVibration()
        controller.midVibration()
        controller.endVibration()
        controller.calibrate()
        controller.setToNormalInputMode()
        var lastOutput: SwitchControllerOutput? = null
        controller.output.subscribe { output ->
            if (output != lastOutput) {
                println(output)
                val addedButtons: Set<SwitchButton> = lastOutput?.let { output.buttons.difference(it.buttons) } ?: output.buttons
                val removedButtons: Set<SwitchButton> = lastOutput?.buttons?.difference(output.buttons) ?: emptySet()
                lastOutput = output
                if (addedButtons.contains(UP) || addedButtons.contains(X)) {
                    controller.vibrate(160f to 320f, 0.3f)
                } else if (removedButtons.contains(UP) || removedButtons.contains(X)) {
                    controller.endVibration()
                }
                addedButtons.forEach {
                    when (it) {
                        Y, LEFT -> controller.setPlayerLight(0)
                        X, UP -> controller.setPlayerLight(1)
                        A, RIGHT -> controller.setPlayerLight(2)
                        B, DOWN -> controller.setPlayerLight(3)
                        else -> { }
                    }
                }
            }
        }
    }
}