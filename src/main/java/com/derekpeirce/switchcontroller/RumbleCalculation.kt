package com.derekpeirce.switchcontroller

import com.derekpeirce.switchcontroller.util.clamp
import com.derekpeirce.switchcontroller.util.lowerByte
import com.derekpeirce.switchcontroller.util.upperByte

/**
 * Translates a description of a desired rumble into the bytes to be sent to the controller.
 * Logic copied from: https://github.com/Looking-Glass/JoyconLib/blob/73dc8f7293ba4ef2c6bc543d77dbf4c32e787aa6/Packages/com.lookingglass.joyconlib/JoyconLib_scripts/Joycon.cs
 */
@ExperimentalUnsignedTypes
fun rumbleData(frequencies: Pair<Float, Float>, amplitude: Float): UByteArray {
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
    return rumbleData
}

@ExperimentalUnsignedTypes
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