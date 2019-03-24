package org.joyconLib

import purejavahidapi.PureJavaHidApi

private val CONTROLLER_IDS = listOf(JoyconConstant.JOYCON_LEFT, JoyconConstant.JOYCON_RIGHT, JoyconConstant.PRO_CONTROLLER)

/**
 * Will connect to controllers when created, then make them available as they're registered.
 */

fun pollDevices(
        acceptDeviceId: (String) -> Boolean = { true },
        acceptProductId: (Short) -> Boolean = { CONTROLLER_IDS.contains(it) }
): List<SwitchController> {
    return PureJavaHidApi.enumerateDevices()
            .map { println(it.manufacturerString + " " + it.deviceId); it }
            .filter { it ->
        JoyconConstant.MANUFACTURER.equals(it.manufacturerString) && it.vendorId == JoyconConstant.VENDOR_ID && acceptProductId(it.productId) && acceptDeviceId(it.deviceId)
    }.map { PureJavaHidApi.openDevice(it) }
            .map { device ->
                val translator = SwitchControllerTranslator(JoyconStickCalc())
                device.setInputReportListener { source, id, data, len ->
                    when (id.toInt()) {
                        0x30 -> // controller input
                            translator.translate(data)
                        0x21 -> { //subcommand
                            when(data[12].toInt()) {
                                -112 -> { // calibration
                                    translator.calibrate(data.sliceArray(19..36).toUByteArray().map { it.toInt() }.toIntArray())
                                }
                            }
                        }
                    }
                }
                SwitchController(device)
            }
}