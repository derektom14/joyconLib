package org.joyconLib

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.SingleEmitter
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import purejavahidapi.HidDevice
import purejavahidapi.InputReportListener
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
class SwitchControllerImpl(
        private val device: HidDevice,
        scheduler: Scheduler
) : SwitchController {

    override val type: SwitchControllerType
        get() = when(device.hidDeviceInfo.productId) {
            JoyconConstant.JOYCON_LEFT -> SwitchControllerType.LEFT_JOYCON
            JoyconConstant.JOYCON_RIGHT -> SwitchControllerType.RIGHT_JOYCON
            JoyconConstant.PRO_CONTROLLER -> SwitchControllerType.PRO_CONTROLLER
            else -> SwitchControllerType.UNKNOWN
        }
    private val translator: SwitchControllerTranslator = SwitchControllerTranslator(JoyconStickCalc(), type)

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

    override fun enableVibration() {
        sendReport(9 to 0x48, 10 to 0x01)
    }

    override fun disableVibration() {
        sendReport(9 to 0x48, 10 to 0x00)
    }

    override fun vibrate(frequencies: Pair<Float, Float>, amplitude: Float, vararg sides: Side) {
        if (sides.isEmpty()) return
        if (amplitude == 0.0f) {
            endVibration()
        } else {
            val lowFreq = frequencies.first.clamp(40.875885f, 626.286133f)
            val amp = amplitude.clamp(0.0f, 1.0f)
            val highFreq = frequencies.second.clamp(81.75177f, 1252.572266f)
            val hfBinary = ((Math.round(32f * Math.log(highFreq * 0.1)) - 0x60) * 4).toUShort()
            val lfByte = (Math.round(32f * Math.log(lowFreq * 0.1)) - 0x40).toUByte()
            val hfAmp = when {
                amp < 0.117 -> (((Math.log(amp.toDouble() * 1000) * 32) - 0x60) / (5 - (amp * amp)) - 1)
                amp < 0.23 -> (((Math.log(amp.toDouble() * 1000) * 32) - 0x60) - 0x5c)
                else -> ((((Math.log(amp.toDouble() * 1000) * 32) - 0x60) * 2) - 0xf6)
            }.toInt().toUByte()
            val lfAmp = calcLfAmp(hfAmp)
            val rumbleData = ubyteArrayOf(
                    hfBinary.lowerByte(),
                    (hfBinary.upperByte() + hfAmp).toUByte(),
                    (lfByte + lfAmp.upperByte()).toUByte(),
                    lfAmp.lowerByte()
            )
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

    private fun calcLfAmp(hfAmp: UByte): UShort {
        var amp = (hfAmp.toInt() * 0.5).toInt() // using integer because unsigned
        val parity = amp % 2
        if (parity > 0) {
            amp -= 1
        }
        amp = (amp / 2) + 0x40
        if (parity > 0) {
            amp = amp or 0x8000
        }
        return amp.toUShort()
    }

    override fun endVibration(vararg sides: Side) {
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