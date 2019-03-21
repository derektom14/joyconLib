/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib.example

import org.joyconLib.Joycon
import org.joyconLib.JoyconConstant

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
    //Set the listener for the Joycon and create a new Listener on the go
    joycon.setListener { je ->
        //Override the method to do what you want with the inputs
        //Navigate in the inputs map
        for ((key, value) in je.newInputs) {
            //Print to the console the name of the button and his state
            print("Button: " + key + " is " + if (value) "ON \t" else "OFF\t")
            //If the button is the capture button it will stop the progam
            if (key == JoyconConstant.CAPTURE) {
                System.exit(0)
                //If the button is the minus button it will close the connection with the joycon
            } else if (key == JoyconConstant.MINUS) {
                joycon.close()
            }
        }
        //Print to the console the position of the joystick
        println("Joystick\tX: " + je.horizontal + "\tY: " + je.vertical)

        println("Battery: " + je.battery)
    }
}