package com.derekpeirce.switchcontroller.util

import java.util.BitSet
import java.util.function.Consumer

/**
 * A set that, like [java.util.EnumSet], uses a bit set to track enums in a set, but in this case
 * directly wraps around a [BitSet] for efficiency
 */
class EnumBitSet<E: Enum<E>> constructor(private val bitSet: BitSet, private val enums: Array<E>) : AbstractSet<E>() {
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
        return if (elements is EnumBitSet) {
            val combined = (bitSet.clone() as BitSet).apply { or(elements.bitSet) }
            bitSet.cardinality() == combined.cardinality()
        } else {
            super.containsAll(elements)
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

    operator fun minus(other: EnumBitSet<E>): EnumBitSet<E> {
        return combineWith(other, BitSet::andNot)
    }

    operator fun plus(other: EnumBitSet<E>): EnumBitSet<E> {
        return combineWith(other, BitSet::or)
    }

    private inline fun combineWith(other: EnumBitSet<E>, combine: BitSet.(BitSet) -> Unit): EnumBitSet<E> {
        return EnumBitSet(bitSet.clone().let { it as BitSet }.apply { this.combine(other.bitSet) }, enums)
    }
}

inline fun <reified E: Enum<E>>enumBitsetOf(bitSet: BitSet) = EnumBitSet(bitSet, E::class.java.enumConstants)

inline fun <reified E : Enum<E>> Iterable<E>.toEnumBitset(): EnumBitSet<E> {
    val enums = E::class.java.enumConstants
    val bitSet = BitSet(enums.size)
    forEach { bitSet.set(it.ordinal) }
    return EnumBitSet(bitSet, enums)
}