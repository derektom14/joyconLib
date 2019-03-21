/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

import kotlin.io.println
import java.io.IOException
import purejavahidapi.DeviceRemovalListener
import purejavahidapi.HidDevice
import purejavahidapi.HidDeviceInfo
import purejavahidapi.InputReportListener
import purejavahidapi.PureJavaHidApi

/**
 * **Main class of the library. This class will be your joycon**
 *
 *
 * To use it, just create a new Joycon object
 *
 *
 * You can check the example to learn how to use this library
 *
 * @author goupil
 * @version 1.0
 */
class Joycon(joyconId: Short) {

    protected var j_Open: Boolean = false
    protected var j_Listener: ((JoyconEvent) -> Unit)? = null
    private var joyconInfo: HidDeviceInfo? = null
    private var joycon: HidDevice? = null
    private var translator: JoyconTranslator? = null
    private var calculator: JoyconStickCalc? = null
    private val factory_stick_cal = IntArray(18)

    init {
        if (joyconId == JoyconConstant.JOYCON_LEFT || joyconId == JoyconConstant.JOYCON_RIGHT) {
            initialize(joyconId)
        } else {
            println("Wrong joycon id!\nPlease use 'JoyconConstant.JOYCON_RIGHT' or 'JoyconConstant.JOYCON_LEFT'")
        }
    }

    /**
     * **Set the listener of the Joycon to handle his input**
     *
     * @param li The listener, specify null to remove it
     */
    fun setListener(listener: (JoyconEvent) -> Unit) {
        j_Listener = listener
    }

    /**
     * **Close the connection with the Joycon**
     *
     * @return True or false if closed correctly
     */
    fun close(): Boolean {
        var isClosed = false
        try {
            joycon!!.close()
            isClosed = true
        } catch (e: IllegalStateException) {
            println("Error while closing conection to the joycon!")
            isClosed = false
        }

        return isClosed
    }

    private fun initialize(joyconId: Short) {
        joyconInfo = null
        joycon = null
        val calculator = JoyconStickCalc()
        this.calculator = calculator
        val type = when (joyconId) {
            JoyconConstant.JOYCON_LEFT -> JoyconType.LEFT
            JoyconConstant.JOYCON_RIGHT -> JoyconType.RIGHT
            else -> throw IllegalArgumentException()
        }
        translator = JoyconTranslator(type, calculator)
        println("Listing Hid devices...")
        val list = PureJavaHidApi.enumerateDevices()
        for (info in list) {
            if (info.manufacturerString == JoyconConstant.MANUFACTURER && info.vendorId == JoyconConstant.VENDOR_ID && info.productId == joyconId) {
                println("Found a Nintendo gear!\nConecting...")
                joyconInfo = info
            }
        }
        if (joyconInfo != null) {
            try {
                joycon = PureJavaHidApi.openDevice(joyconInfo)
                print("Connected to Joy-Con ")
                if (joyconInfo!!.productId == JoyconConstant.JOYCON_LEFT) {
                    println("Left!")
                } else if (joyconInfo!!.productId == JoyconConstant.JOYCON_RIGHT) {
                    println("Right!")
                }

                Thread.sleep(100)

                //Set to HID mode
                val ids: Byte = 1
                var datat = ByteArray(16)
                datat[9] = 0x03
                datat[10] = 0x3F
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(16)

                //Set the joycon user light to blinking
                datat = ByteArray(16)
                datat[9] = 0x30
                datat[10] = 240.toByte()
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(16)

                //Enable vibration
                datat = ByteArray(16)
                datat[9] = 0x48
                datat[10] = 0x01
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(16)

                //Some vibration
                if (joyconInfo!!.productId == JoyconConstant.JOYCON_LEFT) {
                    datat = ByteArray(16)
                    datat[1] = 0xc2.toByte()
                    datat[2] = 0xc8.toByte()
                    datat[3] = 0x03
                    datat[4] = 0x72
                    joycon!!.setOutputReport(ids, datat, 16)

                    Thread.sleep(90)

                    datat = ByteArray(16)
                    datat[1] = 0x00
                    datat[2] = 0x01
                    datat[3] = 0x40
                    datat[4] = 0x40
                    joycon!!.setOutputReport(ids, datat, 16)

                    Thread.sleep(16)

                    datat = ByteArray(16)
                    datat[1] = 0xc3.toByte()
                    datat[2] = 0xc8.toByte()
                    datat[3] = 0x60
                    datat[4] = 0x64
                    joycon!!.setOutputReport(ids, datat, 16)
                } else if (joyconInfo!!.productId == JoyconConstant.JOYCON_RIGHT) {
                    datat = ByteArray(16)
                    datat[5] = 0xc2.toByte()
                    datat[6] = 0xc8.toByte()
                    datat[7] = 0x03
                    datat[8] = 0x72
                    joycon!!.setOutputReport(ids, datat, 16)

                    Thread.sleep(90)

                    datat = ByteArray(16)
                    datat[5] = 0x00
                    datat[6] = 0x01
                    datat[7] = 0x40
                    datat[8] = 0x40
                    joycon!!.setOutputReport(ids, datat, 16)

                    Thread.sleep(16)

                    datat = ByteArray(16)
                    datat[5] = 0xc3.toByte()
                    datat[6] = 0xc8.toByte()
                    datat[7] = 0x60
                    datat[8] = 0x64
                    joycon!!.setOutputReport(ids, datat, 16)
                }

                Thread.sleep(30)

                //Disable vibration
                datat = ByteArray(16)
                datat[9] = 0x48
                datat[10] = 0x00
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(16)

                joycon!!.inputReportListener = object : InputReportListener {

                    private var horizontal = 0f
                    private var vertical = 0f

                    override fun onInputReport(source: HidDevice, id: Byte, data: ByteArray, len: Int) {
                        //Input code case
                        if (id.toInt() == 0x30) {
                            translator!!.translate(data)
                            val newInputs = translator!!.inputs
                            val horizontal = translator!!.horizontal
                            val vertical = translator!!.vertical
                            val battery = translator!!.battery
                            if (j_Listener != null) {
                                if (!newInputs.isEmpty() || horizontal != this.horizontal || vertical != this.vertical) {
                                    j_Listener!!(JoyconEvent(newInputs, horizontal, vertical, battery))
                                    this.horizontal = horizontal
                                    this.vertical = vertical
                                }
                            }
                            //Subcommand code case
                        } else if (id.toInt() == 33) {
                            if (data[12].toInt() == -112) {
                                for (i in 19..36) {
                                    factory_stick_cal[i - 19] = data[i].unsigned()
                                }
                            }
                        }
                    }
                }
                joycon!!.deviceRemovalListener = DeviceRemovalListener { println("Joy-Con disconnected!") }

                //Get joystick calibration info
                datat = ByteArray(16)
                datat[9] = 0x10
                datat[10] = 0x3D
                datat[11] = 0x60
                datat[14] = 0x12
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(100)

                translator!!.calibrate(factory_stick_cal)

                //Set to normal input mode
                datat = ByteArray(16)
                datat[9] = 0x03
                datat[10] = 0x30
                joycon!!.setOutputReport(ids, datat, 16)

                Thread.sleep(16)

            } catch (ex: IOException) {
                println("Error while opening connection to the Joy-Con!\nPlease try to close all software that could communicate with it and retry.")
            } catch (ex: InterruptedException) {
                println("Error, the Joy-Con is not connected anymore!")
            }

        } else {
            println("No Joy-Con was found :(\nTry to conect a Joy-Con and launch the software again.")
        }
    }

}
