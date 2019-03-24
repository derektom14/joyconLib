package org.joyconLib

import com.google.common.base.Optional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import purejavahidapi.PureJavaHidApi
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private val CONTROLLER_IDS = listOf(JoyconConstant.JOYCON_LEFT, JoyconConstant.JOYCON_RIGHT, JoyconConstant.PRO_CONTROLLER)

/**
 * Whenever this observable emits a value, triggers a check for any new controllers.
 * A controller that has been discovered will not be emitted again.
 */
@ExperimentalUnsignedTypes
fun Observable<*>.getSwitchControllers(): Observable<out SwitchController> {
    return this.flatMap {
        Observable.fromIterable(PureJavaHidApi.enumerateDevices())
    }
            .distinct { Pair(it.deviceId, it.productId) }
            .filter {
                JoyconConstant.MANUFACTURER.equals(it.manufacturerString) && it.vendorId == JoyconConstant.VENDOR_ID && CONTROLLER_IDS.contains(it.productId)
            }
            .map { SwitchControllerImpl(PureJavaHidApi.openDevice(it), Schedulers.io()) }
}

fun Observable<out SwitchController>.getPairedSwitchControllers(numPairings: Int = 4, onPair: (SwitchController) -> Unit): Observable<SwitchController> {

    val pairingButtons = listOf(setOf(SwitchButton.L, SwitchButton.R), setOf(SwitchButton.SL_L, SwitchButton.SR_L), setOf(SwitchButton.SL_R, SwitchButton.SR_R))
    val joyconLs = ConcurrentLinkedQueue<SwitchController>()
    val joyconRs = ConcurrentLinkedQueue<SwitchController>()
    val paired = ConcurrentHashMap<SwitchController, Unit>().keySet(Unit)

    fun attemptPairs(controller: SwitchController, prevButtons: Set<SwitchButton>, buttons: Set<SwitchButton>): Maybe<SwitchController> {
        return when (controller.type) {
            SwitchControllerType.LEFT_JOYCON -> attemptPair(controller, prevButtons.contains(SwitchButton.L), buttons.contains(SwitchButton.L), joyconLs, joyconRs) { left, right ->
                paired.add(right)
                JoyconPair(left, right)
            }
            SwitchControllerType.RIGHT_JOYCON -> attemptPair(controller, prevButtons.contains(SwitchButton.R), buttons.contains(SwitchButton.R), joyconRs, joyconLs) { right, left ->
                paired.add(left)
                JoyconPair(left, right)
            }
            else -> Maybe.empty()
        }
    }

    return this.flatMapMaybe {
        it.setToHidMode()
        it.setLightsBlinking()
        it.calibrate()
        it.setToNormalInputMode()
        it.output
                .distinctUntilChanged()
                .buffer(2, 1)
                .flatMapMaybe { (prevOutput, output) ->
                    if (pairingButtons.any { output.buttons.containsAll(it) }) {
                        Maybe.just(Optional.of(it))
                    } else if (paired.remove(it)) {
                        Maybe.just(Optional.absent())
                    } else {
                        attemptPairs(it, prevOutput.buttons, output.buttons).map { Optional.of(it) }
                    }
                }
                .firstElement()
                .flatMap { it.orNull()?.let { Maybe.just(it) } ?: Maybe.empty() }
    }.zipWith<Int, SwitchController> (Observable.fromIterable(0 until numPairings), BiFunction { controller, index ->
        controller.setPlayerLight(index)
        onPair(controller)
        controller
     })
}

private fun attemptPair(
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
        } else {
            val other = otherSet.remove()
            return Maybe.just(pair(controller, other))
        }
    } else if (wasPressed && !isPressed) {
        selfSet.remove(controller)
    }
    return Maybe.empty<SwitchController>()
}