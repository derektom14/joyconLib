package com.derekpeirce.switchcontroller

import com.derekpeirce.switchcontroller.util.EnumBitSet
import com.derekpeirce.switchcontroller.util.enumBitsetOf
import io.reactivex.Observable
import java.awt.geom.Point2D
import java.util.BitSet

/**
 * The current output from a controller.
 * [buttons] contains all currently pressed buttons.
 * [battery] contains a list of battery information.
 * [leftStick] and [rightStick] contain Joystick positions, null if the stick doesn't exist for this controller
 */
data class SwitchControllerOutput(
        val buttons: EnumBitSet<SwitchButton>,
        val battery: List<Int>,
        val leftStick: Point2D.Float?,
        val rightStick: Point2D.Float?
) {
    companion object {
        val EMPTY = SwitchControllerOutput(
                enumBitsetOf(BitSet()),
                listOf(),
                Point2D.Float(),
                Point2D.Float()
        )
    }

    /**
     * The changes between this and the other output
     */
    operator fun minus(other: SwitchControllerOutput): Difference {
        return Difference(
                buttons - other.buttons,
                other.buttons - this.buttons,
                if (leftStick != other.leftStick) leftStick else null,
                if (rightStick != other.rightStick) rightStick else null
        )
    }

    /**
     * [pressedButtons] contains buttons that are newly pressed.
     * [releasedButtons] contains buttons that are newly released.
     * [leftStick] and [rightStick] are present if changed, and null if not changed.
     */
    data class Difference(
            val pressedButtons: Set<SwitchButton>,
            val releasedButtons: Set<SwitchButton>,
            val leftStick: Point2D.Float?,
            val rightStick: Point2D.Float?
    ) {
        /**
         * True if there was no change in output
         */
        fun isEmpty() = pressedButtons.isEmpty() && releasedButtons.isEmpty() && leftStick == null && rightStick == null

        override fun toString(): String {
            val content = mutableListOf<String>()
            if (pressedButtons.isNotEmpty()) content += "pressedButtons=$pressedButtons"
            if (releasedButtons.isNotEmpty()) content += "releasedButtons=$releasedButtons"
            if (leftStick != null) content += "leftStick=$leftStick"
            if (rightStick != null) content += "rightStick=$rightStick"
            return "Difference(${content.joinToString(", ")})"
        }

        /**
         * Returns true if the button is newly pressed, false if it is newly released, and null otherwise
         */
        fun checkButton(button: SwitchButton): Boolean? {
            return if (pressedButtons.contains(button)) {
                true
            } else if (releasedButtons.contains(button)) {
                false
            } else {
                null
            }
        }
    }
}

/**
 * Converts a stream of controller outputs into the differences between those outputs, where they exist
 */
fun Observable<SwitchControllerOutput>.differences(): Observable<SwitchControllerOutput.Difference> {
    return buffer(2, 1)
            .map { (previous, next) -> next - previous}
            .filter { !it.isEmpty() }
}