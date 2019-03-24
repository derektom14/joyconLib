package org.joyconLib

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import purejavahidapi.PureJavaHidApi

private val CONTROLLER_IDS = listOf(JoyconConstant.JOYCON_LEFT, JoyconConstant.JOYCON_RIGHT, JoyconConstant.PRO_CONTROLLER)

/**
 * Will connect to controllers when created, then make them available as they're registered.
 */

fun pollDevices(
        acceptDeviceId: (String) -> Boolean = { true },
        acceptProductId: (Short) -> Boolean = { CONTROLLER_IDS.contains(it) },
        scheduler: Scheduler = Schedulers.io()
): List<SwitchController> {
    return PureJavaHidApi.enumerateDevices()
            .map { println(it.manufacturerString + " " + it.deviceId); it }
            .filter { it ->
        JoyconConstant.MANUFACTURER.equals(it.manufacturerString) && it.vendorId == JoyconConstant.VENDOR_ID && acceptProductId(it.productId) && acceptDeviceId(it.deviceId)
    }.map { PureJavaHidApi.openDevice(it) }
            .map { device ->
                SwitchController(device, scheduler)
            }
}