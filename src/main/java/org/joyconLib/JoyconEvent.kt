/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

/**
 * **The object that will be sent when an input is triggered**
 *
 *
 * **newInputs** contains the new inputs with this format:
 * **NAME_OF_THE_INPUT**:**STATE_OF_THE_INPUT** (true/false)
 *
 *
 *
 * **joystick** contains the value of the joystick (0 - 8)
 *
 * @version 1.0
 * @author goupil
 */
class JoyconEvent(val newInputs: Map<String, Boolean>, val horizontal: Float, val vertical: Float, val battery: Byte)
