package com.derekpeirce.switchcontroller.example

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import com.derekpeirce.switchcontroller.Side
import com.derekpeirce.switchcontroller.SwitchButton.*
import com.derekpeirce.switchcontroller.SwitchControllerOutput
import com.derekpeirce.switchcontroller.differences
import com.derekpeirce.switchcontroller.getPairedSwitchControllers
import com.derekpeirce.switchcontroller.getSwitchControllers
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
fun main() {

    Observable.interval(0L, 5, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .getSwitchControllers()
            .doOnNext { println("Found controller: $it") }
            .getPairedSwitchControllers { it.rumble(160f to 320f, 0.3f) }
            .doOnNext { println("Paired controller: $it") }
            .flatMap { controller ->
                controller.horizontalOutput
                        .startWith(SwitchControllerOutput.EMPTY)
                        .differences()
                        .doOnNext {
                            println("Output change for controller $controller: $it")
                            when (it.checkButton(X)) {
                                true -> controller.rumble(160f to 320f, 0.5f, Side.RIGHT)
                                false -> controller.endRumble(Side.RIGHT)
                            }
                            when (it.checkButton(UP)) {
                                true -> controller.rumble(160f to 320f, 0.5f, Side.LEFT)
                                false -> controller.endRumble(Side.LEFT)
                            }
                        }
            }
            .blockingLast()
}