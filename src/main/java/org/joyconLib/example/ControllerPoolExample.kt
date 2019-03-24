package org.joyconLib.example

import com.google.common.base.Optional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.joyconLib.JoyconPair
import org.joyconLib.SwitchButton
import org.joyconLib.SwitchButton.*
import org.joyconLib.SwitchController
import org.joyconLib.SwitchControllerType
import org.joyconLib.pollDevices
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

fun main() {
    val controllers = pollDevices()
    println(controllers)

    val pairingButtons = listOf(setOf(L, R), setOf(SL_L, SR_L), setOf(SL_R, SR_R))

    val numbers = Observable.just(0, 1, 2, 3)

    val joyconLs = ConcurrentLinkedQueue<SwitchController>()
    val joyconRs = ConcurrentLinkedQueue<SwitchController>()
    val paired = ConcurrentHashMap<SwitchController, Unit>().keySet(Unit)

    fun attemptPair(
            controller: SwitchController,
            wasPressed: Boolean,
            isPressed: Boolean,
            selfSet: ConcurrentLinkedQueue<SwitchController>,
            otherSet: ConcurrentLinkedQueue<SwitchController>,
            pair: (SwitchController, SwitchController) -> SwitchController
    ): Maybe<SwitchController> {
        if (!wasPressed && isPressed) {
            if (otherSet.isEmpty()) {
                selfSet.add(controller)
                println("Waiting: $selfSet, $otherSet")
            } else {
                val other = otherSet.remove()
                paired.add(other)
                println("Paired $controller and $other")
                return Maybe.just(pair(controller, other))
            }
        } else if (wasPressed && !isPressed) {
            selfSet.remove(controller)
        }
        return Maybe.empty<SwitchController>()
    }

    fun attemptPairs(controller: SwitchController, prevButtons: Set<SwitchButton>, buttons: Set<SwitchButton>): Maybe<SwitchController> {
        return when (controller.type) {
            SwitchControllerType.LEFT_JOYCON -> attemptPair(controller, prevButtons.contains(L), buttons.contains(L), joyconLs, joyconRs) {
                left, right -> JoyconPair(left, right)
            }
            SwitchControllerType.RIGHT_JOYCON -> attemptPair(controller, prevButtons.contains(R), buttons.contains(R), joyconRs, joyconLs) {
                right, left -> JoyconPair(left, right)
            }
            else -> Maybe.empty()
        }
    }


    Observable.fromIterable(controllers).flatMapMaybe { controller ->
        controller.setToHidMode()
        controller.setLightsBlinking()
        controller.calibrate()
        controller.setToNormalInputMode()
        controller.output
                .distinctUntilChanged()
                .doOnEach { println("Waiting for: $it") }
                .buffer(2, 1)
                .flatMapMaybe { (prevOutput, output) ->
                    if (pairingButtons.any { output.buttons.containsAll(it) }) {
                        Maybe.just(Optional.of(controller))
                    } else if (paired.remove(controller)) {
                        Maybe.just(Optional.absent())
                    } else {
                        attemptPairs(controller, prevOutput.buttons, output.buttons).map { Optional.of(it) }
                    }
                }
                .firstElement()
                .flatMap { Maybe.fromCallable { it.orNull() } }

    }
            .zipWith<Int, Pair<SwitchController, Int>>(numbers, BiFunction { t1, t2 -> t1 to t2 })
            .flatMap { (controller, index) ->
                controller.setPlayerLight(index)
                controller.vibrate(160f to 320f, 0.3f)
                println("Paired $controller $index")
                controller.output
                        .distinctUntilChanged()
                        .doOnNext {
                            println("True controller $it")
                        }
            }.subscribe()


}