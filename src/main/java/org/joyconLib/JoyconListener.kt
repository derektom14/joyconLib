/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joyconLib

/**
 * **The listener for the joycon**
 *
 *
 * Give this listener to the joycon to handle his inputs
 *
 *
 * Refer to the example to learn how to use it
 *
 * @version 1.0
 * @author goupil
 */
interface JoyconListener {

    /**
     *
     *
     * @param e The event object
     */
    fun handleNewInput(e: JoyconEvent)
}
