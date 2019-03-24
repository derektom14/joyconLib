package org.joyconLib.example

import com.google.common.base.Optional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.joyconLib.JoyconPair
import org.joyconLib.Side
import org.joyconLib.SwitchButton
import org.joyconLib.SwitchButton.*
import org.joyconLib.SwitchController
import org.joyconLib.SwitchControllerOutput
import org.joyconLib.SwitchControllerType
import org.joyconLib.differences
import org.joyconLib.getPairedSwitchControllers
import org.joyconLib.getSwitchControllers
import org.joyconLib.pollDevices
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
fun main() {

    Observable.interval(0L, 5, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .getSwitchControllers()
            .doOnNext { println("Found controller: $it") }
            .getPairedSwitchControllers { it.vibrate(160f to 320f, 0.3f) }
            .doOnNext { println("Paired controller: $it") }
            .flatMap { controller ->
                controller.output
                        .startWith(SwitchControllerOutput.EMPTY)
                        .differences()
                        .doOnNext {
                            println("Output change for controller $controller: $it")
                            when (it.checkButton(X)) {
                                true -> controller.vibrate(160f to 320f, 0.5f, Side.RIGHT)
                                false -> controller.endVibration(Side.RIGHT)
                            }
                            when (it.checkButton(UP)) {
                                true -> controller.vibrate(160f to 320f, 0.5f, Side.LEFT)
                                false -> controller.endVibration(Side.LEFT)
                            }
                        }
            }
            .blockingLast()
}