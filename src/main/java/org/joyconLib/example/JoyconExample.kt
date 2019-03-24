/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib.example

import org.joyconLib.Joycon
import org.joyconLib.JoyconConstant
import org.joyconLib.SwitchControllerOutput

/**
 * **The example of the JoyconLib**
 *
 *
 * This example will try to connect to the left joycon and print on the
 * console the inputs that are triggered
 *
 *
 *
 * Comment are in the method to learn how it work
 */
fun main(args: Array<String>) {
    //Create a new Joycon with the identifier of the left joycon
    val joycon = Joycon(JoyconConstant.JOYCON_LEFT)

    var oldData: SwitchControllerOutput? = null
    //Set the listener for the Joycon and create a new Listener on the go
    joycon.setListener { je ->
        //Override the method to do what you want with the inputs
        if (je != oldData) {
            println("Data: $je")
            oldData = je
        }
    }
}