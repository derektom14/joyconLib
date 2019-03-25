package com.derekpeirce.switchcontroller

import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import com.derekpeirce.switchcontroller.SwitchButton.*
import com.derekpeirce.switchcontroller.util.EnumBitSet
import com.derekpeirce.switchcontroller.util.rotateLeft90
import com.derekpeirce.switchcontroller.util.rotateRight90
import com.derekpeirce.switchcontroller.util.toEnumBitset
import purejavahidapi.HidDevice
import purejavahidapi.InputReportListener
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
class SwitchControllerImpl(
        private val device: HidDevice,
        override val type: SwitchControllerType,
        scheduler: Scheduler
) : SwitchController {

    private val translator: SwitchControllerTranslator = SwitchControllerTranslator(type)

    private val reportSubject: Subject<ByteArray> = PublishSubject.create()

    override val output = Observable.create<SwitchControllerOutput> { emitter ->
        device.inputReportListener = object : InputReportListener {

            override fun onInputReport(source: HidDevice, id: Byte, data: ByteArray, len: Int) {
                //Input code case
                if (id.toInt() == 0x30) {
                    val newData = translator.translate(data)
                    if (!emitter.isDisposed) {
                        emitter.onNext(newData)
                    }
                    //Subcommand code case
                } else if (id.toInt() == 33) {
                    if (data[12].toInt() == -112) {
                        val factory_stick_cal = IntArray(18)
                        for (i in 19..36) {
                            factory_stick_cal[i - 19] = data[i].toUByte().toInt()
                        }
                        translator.calibrate(factory_stick_cal)
                    }
                }
            }
        }

        device.setDeviceRemovalListener {
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
    }.share()


    override val horizontalOutput: Observable<SwitchControllerOutput>
        get() = output.map {
            when (type) {
                SwitchControllerType.LEFT_JOYCON -> SwitchControllerOutput(mapFromLeft(it.buttons), it.battery, it.leftStick?.rotateLeft90(), null)
                SwitchControllerType.RIGHT_JOYCON -> SwitchControllerOutput(mapFromRight(it.buttons), it.battery, it.rightStick?.rotateRight90(), null)
                else -> it
            }
        }

    private fun mapFromLeft(buttons: EnumBitSet<SwitchButton>): EnumBitSet<SwitchButton> {
        return buttons.map {
            when (it) {
                RIGHT -> X
                UP -> Y
                LEFT -> B
                DOWN -> A
                SL_L -> L
                SR_L -> R
                else -> it
            }
        }.toEnumBitset()
    }

    private fun mapFromRight(buttons: EnumBitSet<SwitchButton>): EnumBitSet<SwitchButton> {
        return buttons.map {
            when (it) {
                Y -> X
                B -> Y
                A -> B
                X -> A
                SL_R -> L
                SR_R -> R
                else -> it
            }
        }.toEnumBitset()
    }

    init {
        reportSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .zipWith<Long, ByteArray>(Observable.interval(16, TimeUnit.MILLISECONDS).toFlowable(BackpressureStrategy.LATEST), BiFunction { s: ByteArray, _: Long -> s})
                .observeOn(scheduler)
                .subscribe {
                    device.setOutputReport(1, it, 16)
                }
    }

    override fun toString(): String {
        return "$type:${device.hidDeviceInfo.deviceId}"
    }

    override fun setToHidMode() {
        sendReport(9 to 0x03, 10 to 0x3F)
    }

    override fun setToNormalInputMode() {
        sendReport(9 to 0x03, 10 to 0x30)
    }

    override fun setLightsBlinking() {
        sendReport(9 to 0x30, 10 to 0b1111_0000.toByte())
    }

    override fun setLightsOff() {
        sendReport(9 to 0x30, 10 to 0b0000_0000)
    }

    override fun enableRumble() {
        sendReport(9 to 0x48, 10 to 0x01)
    }

    override fun disableRumble() {
        sendReport(9 to 0x48, 10 to 0x00)
    }

    override fun rumble(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side) {
        if (sides.isEmpty()) return
        if (amplitude == 0.0f) {
            endRumble()
        } else {
            val rumbleData = rumbleData(frequencies, amplitude)
            val report = UByteArray(16)
            sides.map {
                when(it) {
                    Side.LEFT -> 1
                    Side.RIGHT -> 5
                }
            }.forEach {
                report[it] = rumbleData[0]
                report[it+1] = rumbleData[1]
                report[it+2] = rumbleData[2]
                report[it+3] = rumbleData[3]
            }

            reportSubject.onNext(report.toByteArray())

        }
    }

    override fun endRumble(vararg sides: Side) {
        val left = if (sides.contains(Side.LEFT)) {
            listOf<Pair<Int, Byte>>(
                    1 to 0x0,
                    2 to 0x1,
                    3 to 0x40,
                    4 to 0x40
            )
        } else {
            emptyList()
        }
        val right = if (sides.contains(Side.RIGHT)) {
            listOf<Pair<Int, Byte>>(
                    5 to 0x0,
                    6 to 0x1,
                    7 to 0x40,
                    8 to 0x40
            )
        } else {
            emptyList()
        }
        sendReport(*(left + right).toTypedArray())
    }

    override fun calibrate() {
        sendReport(
                9 to 0x10,
                10 to 0x3D,
                11 to 0x60,
                14 to 0x12
        )
    }

    private fun sendReport(vararg bytes: Pair<Int, Byte>) {
        val datat = ByteArray(16)
        bytes.forEach { (i, value) -> datat[i] = value }
        reportSubject.onNext(datat)
    }

    override fun setPlayerLight(i: Int) {
        sendReport(
                9 to 0x30,
                10 to when(i) {
                    in 0..3 -> (1 shl i)
                    else -> throw IllegalArgumentException("Invalid player number $i")
                }.toByte()
        )
    }
}