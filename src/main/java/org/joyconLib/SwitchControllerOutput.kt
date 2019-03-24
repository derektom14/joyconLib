package org.joyconLib

import io.reactivex.Observable
import java.awt.geom.Point2D
import java.util.BitSet

data class SwitchControllerOutput(
        val buttons: EnumBitset<SwitchButton>,
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

    operator fun minus(other: SwitchControllerOutput): Difference {
        return Difference(
                buttons - other.buttons,
                other.buttons - this.buttons,
                if (leftStick != other.leftStick) leftStick else null,
                if (rightStick != other.rightStick) rightStick else null
        )
    }

    data class Difference(
            val pressedButtons: Set<SwitchButton>,
            val releasedButtons: Set<SwitchButton>,
            val leftStick: Point2D.Float?,
            val rightStick: Point2D.Float?
    ) {
        fun isEmpty() = pressedButtons.isEmpty() && releasedButtons.isEmpty() && leftStick == null && rightStick == null

        override fun toString(): String {
            val content = mutableListOf<String>()
            if (pressedButtons.isNotEmpty()) content += "pressedButtons=$pressedButtons"
            if (releasedButtons.isNotEmpty()) content += "releasedButtons=$releasedButtons"
            if (leftStick != null) content += "leftStick=$leftStick"
            if (rightStick != null) content += "rightStick=$rightStick"
            return "Difference(${content.joinToString(", ")})"
        }

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