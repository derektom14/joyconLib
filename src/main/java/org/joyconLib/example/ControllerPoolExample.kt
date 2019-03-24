package org.joyconLib.example

import org.joyconLib.SwitchButton
import org.joyconLib.SwitchControllerOutput
import org.joyconLib.SwitchButton.*
import org.joyconLib.pollDevices
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val controllers = pollDevices()
    println(controllers)

    controllers.forEach { controller ->
        Thread.sleep(100)
        controller.setToHidMode()
        Thread.sleep(50)
        controller.setLightsBlinking()
        Thread.sleep(50)
        controller.enableVibration()
        Thread.sleep(50)
        controller.startVibration()
        Thread.sleep(500)
        controller.midVibration()
        Thread.sleep(50)
        controller.endVibration()
        Thread.sleep(50)
        controller.calibrate()
        Thread.sleep(50)
        controller.setToNormalInputMode()
        Thread.sleep(50)
        var lastOutput: SwitchControllerOutput? = null
        controller.listener = { output ->
            if (output != lastOutput) {
                println(output)
                val addedButtons: Set<SwitchButton> = lastOutput?.let { output.buttons.difference(it.buttons) } ?: output.buttons
                val removedButtons: Set<SwitchButton> = lastOutput?.buttons?.difference(output.buttons) ?: emptySet()
                lastOutput = output
                if (addedButtons.contains(UP)) {
                    controller.vibrate(160f to 320f, 0.6f, 200, TimeUnit.MILLISECONDS)
//                    controller.startVibration()
                } else if (removedButtons.contains(UP)) {
                    controller.endVibration()
                }
                addedButtons.forEach {
                    when (it) {
                        Y -> controller.setPlayerLight(0)
                        X -> controller.setPlayerLight(1)
                        A -> controller.setPlayerLight(2)
                        B -> controller.setPlayerLight(3)
                        else -> run { }
                    }
                }
            }
        }
    }
}