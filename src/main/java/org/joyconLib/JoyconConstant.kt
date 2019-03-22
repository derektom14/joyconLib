/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

/**
 * **The class with all the constant value the library needs to work**
 *
 * @version 1.0
 * @author goupil
 */
object JoyconConstant {

    //Other Values
    val VENDOR_ID: Short = 0x057E
    val MANUFACTURER = "Nintendo"
    val JOYCON_LEFT: Short = 0x2006
    val JOYCON_RIGHT: Short = 0x2007
    val PRO_CONTROLLER: Short = 0x2009

    //Shared Values
    val MINUS = "MINUS"
    val MINUS_ON = 1
    val MINUS_OFF = -MINUS_ON

    val PLUS = "PLUS"
    val PLUS_ON = 2
    val PLUS_OFF = -PLUS_ON

    val RIGHT_STICK = "RIGHT_STICK"
    val RIGHT_STICK_ON = 4
    val RIGHT_STICK_OFF = -RIGHT_STICK_ON

    val LEFT_STICK = "LEFT_STICK"
    val LEFT_STICK_ON = 8
    val LEFT_STICK_OFF = -LEFT_STICK_ON

    val HOME = "HOME"
    val HOME_ON = 16
    val HOME_OFF = -HOME_ON

    val CAPTURE = "CAPTURE"
    val CAPTURE_ON = 32
    val CAPTURE_OFF = -CAPTURE_ON

    val SR = "SR"
    val SR_ON = 16
    val SR_OFF = -SR_ON

    val SL = "SL"
    val SL_ON = 32
    val SL_OFF = -SL_ON

    //Left Joy-Con Values
    val DOWN = "DOWN"
    val DOWN_ON = 1
    val DOWN_OFF = -DOWN_ON

    val UP = "UP"
    val UP_ON = 2
    val UP_OFF = -UP_ON

    val RIGHT = "RIGHT"
    val RIGHT_ON = 4
    val RIGHT_OFF = -RIGHT_ON

    val LEFT = "LEFT"
    val LEFT_ON = 8
    val LEFT_OFF = -LEFT_ON

    val L = "L"
    val L_ON = 64
    val L_OFF = -L_ON

    val ZL = "ZL"
    val ZL_ON = 128
    val ZL_OFF = -ZL_ON

    //Right Joy-Con Values
    val Y = "Y"
    val Y_ON = 1
    val Y_OFF = -Y_ON

    val X = "X"
    val X_ON = 2
    val X_OFF = -X_ON

    val B = "B"
    val B_ON = 4
    val B_OFF = -B_ON

    val A = "A"
    val A_ON = 8
    val A_OFF = -A_ON

    val R = "R"
    val R_ON = 64
    val R_OFF = -R_ON

    val ZR = "ZR"
    val ZR_ON = 128
    val ZR_OFF = -ZR_ON
}
