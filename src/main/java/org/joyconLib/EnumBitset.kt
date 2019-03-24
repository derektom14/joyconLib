package org.joyconLib

import java.util.Arrays
import java.util.BitSet
import java.util.function.Consumer

class EnumBitset<E: Enum<E>> constructor(private val bitSet: BitSet, private val enums: Array<E>) : AbstractSet<E>() {
    override val size: Int
        get() = bitSet.cardinality()

    override fun iterator(): Iterator<E> {
        return object : AbstractIterator<E>() {
            var index = -1
            override fun computeNext() {
                index = bitSet.nextSetBit(index + 1)
                if (index >= bitSet.length() || index < 0) {
                    done()
                } else {
                    setNext(enums[index])
                }
            }
        }
    }

    override fun contains(element: E): Boolean {
        return bitSet.get(element.ordinal)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return if (elements is EnumBitset) {
            val combined = (bitSet.clone() as BitSet).apply { or(elements.bitSet) }
            bitSet.cardinality() == combined.cardinality()
        } else {
            super.containsAll(elements)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is EnumBitset<*>) {
            other.bitSet == this.bitSet && Arrays.equals(other.enums, this.enums)
        } else {
            super.equals(other)
        }
    }

    override fun forEach(action: Consumer<in E>?) {
        var i = bitSet.nextSetBit(0)
        while (i != -1) {
            action!!.accept(enums[i])
            i = bitSet.nextSetBit(i + 1)
        }
    }

    override fun isEmpty(): Boolean {
        return bitSet.isEmpty
    }

    operator fun minus(other: EnumBitset<E>): EnumBitset<E> {
        return combineWith(other, BitSet::andNot)
    }

    operator fun plus(other: EnumBitset<E>): EnumBitset<E> {
        return combineWith(other, BitSet::or)
    }

    private inline fun combineWith(other: EnumBitset<E>, combine: BitSet.(BitSet) -> Unit): EnumBitset<E> {
        return EnumBitset(bitSet.clone().let { it as BitSet }.apply { this.combine(other.bitSet) }, enums)
    }
}

inline fun <reified E: Enum<E>>enumBitsetOf(bitSet: BitSet) = EnumBitset(bitSet, E::class.java.enumConstants)

inline fun <reified E : Enum<E>> Iterable<E>.toEnumBitset(): EnumBitset<E> {
    val enums = E::class.java.enumConstants
    val bitSet = BitSet(enums.size)
    forEach { bitSet.set(it.ordinal) }
    return EnumBitset(bitSet, enums)
}