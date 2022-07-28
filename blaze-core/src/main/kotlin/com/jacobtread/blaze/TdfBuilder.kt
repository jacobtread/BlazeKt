package com.jacobtread.blaze

import com.jacobtread.blaze.data.VarPair
import com.jacobtread.blaze.data.VarTriple
import com.jacobtread.blaze.tdf.*
import com.jacobtread.blaze.tdf.types.ByteTdf
import com.jacobtread.blaze.tdf.types.ShortTdf
import com.jacobtread.blaze.tdf.types.ULongTdf
import com.jacobtread.blaze.tdf.types.*

/**
 * TdfBuilder Builder class used to create Tdf structures easily
 * rather than having to create all the objects manually the helper
 * methods on this class can be used instead which automatically adds
 * the correct Tdf value to the values list which is later written
 * as the packet content or used as struct values depending on context
 *
 * @constructor Create empty TdfBuilder
 */
class TdfBuilder {

    companion object {
        val EMPTY_BYTE_ARRAY = ByteArray(0)
    }

    val values = ArrayList<Tdf<*>>()

    /**
     * text Adds a new text value to the builder. This
     * becomes a StringTdf when created
     *
     * @param label The label of the Tdf
     * @param value The string value of the Tdf
     */
    fun text(label: String, value: String = "") {
        values.add(StringTdf(label, value))
    }


    /**
     * number Adds a new number value to the builder.
     * This becomes a VarInt when created
     *
     * @param label The label of the Tdf
     * @param value The long value of the Tdf
     */
    fun number(label: String, value: ULong) {
        if (value <= 127u) {
            values.add(ByteTdf(label, (value and 0xFFu).toByte()))
        } else if (value <= 255u) {
            values.add(UByteTdf(label, value.toUByte()))
        } else if (value <= 32767u) {
            values.add(ShortTdf(label, (value and 0xFFFFu).toShort()))
        } else {
            values.add(ULongTdf(label, value))
        }
    }

    /**
     * number Adds a new number value to the builder.
     * This becomes a VarInt when created
     *
     * @param label The label of the Tdf
     * @param value The int value of the Tdf
     */
    fun number(label: String, value: UInt) {
        if (value <= 127u) {
            values.add(ByteTdf(label, (value and 0xFFu).toByte()))
        } else if (value <= 255u) {
            values.add(UByteTdf(label, value.toUByte()))
        } else if (value <= 32767u) {
            values.add(ShortTdf(label, (value and 0xFFFFu).toShort()))
        } else {
            values.add(ULongTdf(label, value.toULong()))
        }
    }

    fun number(label: String, value: Int) {
        if (value <= 127) {
            values.add(ByteTdf(label, (value and 0xFF).toByte()))
        } else if (value <= 255) {
            values.add(UByteTdf(label, value.toUByte()))
        } else if (value <= 32767) {
            values.add(ShortTdf(label, (value and 0xFFFF).toShort()))
        } else {
            values.add(ULongTdf(label, value))
        }
    }

    fun number(label: String, value: Long) {
        if (value <= 127) {
            values.add(ByteTdf(label, (value and 0xFF).toByte()))
        } else if (value <= 255) {
            values.add(UByteTdf(label, value.toUByte()))
        } else if (value <= 32767) {
            values.add(ShortTdf(label, (value and 0xFFFF).toShort()))
        } else {
            values.add(ULongTdf(label, value.toULong()))
        }
    }

    /**
     * bool Adds a numerical representation of the boolean value (0x0 or 0x1) as
     * a var int
     *
     * @param label The label of the Tdf
     * @param value The boolean value of the Tdf true = 0x1 false = 0x0
     */
    fun bool(label: String, value: Boolean) {
        values.add(ByteTdf(label, if (value) 1 else 0))
    }


    /**
     * blob Adds a new blob value to the builder.
     * This becomes a BlobTdf when created
     *
     * @param label The label of the Tdf
     * @param value The byte array to be used as the blob
     */
    fun blob(label: String, value: ByteArray = EMPTY_BYTE_ARRAY) {
        values.add(BlobTdf(label, value))
    }

    /**
     * tripple Adds a new tripple value to the builder.
     * This becomes a TrippleTdf when created
     *
     * @param label The label of the Tdf
     * @param a The first value of the tripple
     * @param b The second value of the tripple
     * @param c The third value of the tripple
     */
    fun tripple(label: String, a: Long, b: Long, c: Long) {
        values.add(TrippleTdf(label, VarTriple(a.toULong(), b.toULong(), c.toULong())))
    }

    fun tripple(label: String, a: ULong, b: ULong, c: ULong) {
        values.add(TrippleTdf(label, VarTriple(a, b, c)))
    }

    fun tripple(label: String, a: Int, b: Int, c: Int) {
        values.add(TrippleTdf(label, VarTriple(a.toULong(), b.toULong(), c.toULong())))
    }

    /**
     * tripple Adds a new tripple value to the builder.
     * This becomes a TrippleTdf when created
     *
     * @param label The label of the Tdf
     * @param value The tripple value
     */
    fun tripple(label: String, value: VarTriple) {
        values.add(TrippleTdf(label, value))
    }

    /**
     * pair Adds a new pair of values to the builder.
     * This becomes a PairTdf when created
     *
     * @param label The label of the Tdf
     * @param a The first value of the pair
     * @param b The second value of the pair
     */
    fun pair(label: String, a: Long, b: Long) {
        values.add(PairTdf(label, VarPair(a.toULong(), b.toULong())))
    }

    fun pair(label: String, a: ULong, b: ULong) {
        values.add(PairTdf(label, VarPair(a, b)))
    }

    fun pair(label: String, a: Int, b: Int) {
        values.add(PairTdf(label, VarPair(a.toULong(), b.toULong())))
    }


    /**
     * pair Adds a new pair of values to the builder.
     * This becomes a PairTdf when created
     *
     * @param label The label of the Tdf
     * @param value The pair of values
     */
    fun pair(label: String, value: VarPair) {
        values.add(PairTdf(label, value))
    }


    /**
     * float Adds a new float value to the builder.
     * This becomes a FloatTdf when created
     *
     * @param label The label of the Tdf
     * @param value The float value
     */
    fun float(label: String, value: Float) {
        values.add(FloatTdf(label, value))
    }

    /**
     * list Adds a new list value to the builder.
     * This becomes a ListTdf when created
     *
     * @param label The label of the Tdf
     * @param value The list value
     */
    inline fun <reified A : Any> list(label: String, value: List<A>) {
        list(label, Tdf.getTypeFromClass(A::class.java), value)
    }

    fun list(label: String, type: UByte, value: List<Any>) {
        values.add(ListTdf(label, type, value))
    }

    /**
     * list Adds a new list value to the builder.
     * This becomes a ListTdf when created
     *
     * @param label The label of the Tdf
     * @param values The values to create the list from
     */
    inline fun <reified A : Any> list(label: String, vararg values: A) {
        val type = Tdf.getTypeFromClass(A::class.java)
        this.values.add(ListTdf(label, type, values.toList()))
    }


    /**
     * map Adds a new map value to the builder.
     * This becomes a MapTdf when created
     *
     * @param label The label of the Tdf
     * @param value The map value
     */
    inline fun <reified A : Any, reified B : Any> map(label: String, value: Map<A, B>) {
        map(label, Tdf.getTypeFromClass(A::class.java), Tdf.getTypeFromClass(B::class.java), value)
    }

    fun map(label: String, keyType: UByte, valueType: UByte, value: Map<*, *>) {
        values.add(MapTdf(label, keyType, valueType, value))
    }


    /**
     * varList Adds a new var int list value to the builder.
     * This becomes VarListTdf when created
     *
     * @param label The label of the Tdf
     * @param value The list value
     */
    fun varList(label: String, value: List<ULong> = emptyList()) {
        values.add(VarIntListTdf(label, value))
    }

    /**
     * union Adds a new union value to the builder.
     * This becomes a UnionTdf when created
     *
     * @param label The label of the Tdf
     * @param type The type of union
     * @param value The value of the union
     */
    fun optional(label: String, type: UByte = OptionalTdf.NO_VALUE_TYPE, value: Tdf<*>? = null) {
        values.add(OptionalTdf(label, type, value))
    }

    /**
     * union Adds a new union value to the builder.
     * This becomes a UnionTdf when created
     *
     * @param label The label of the Tdf
     * @param value The value of the union
     * @param type The type of union
     */
    fun optional(label: String, value: Tdf<*>, type: UByte = 0x0u) {
        values.add(OptionalTdf(label, type, value))
    }

    /**
     * unaryPlus Overriding the + modifier so that structs can
     * be added to the values using
     * ```
     * +struct("LABEL") {}
     * ```
     */
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun Tdf<*>.unaryPlus() {
        values.add(this)
    }

}

/**
 * struct Creates a new struct tdf element
 *
 * @param label The label of this struct
 * @param start2 Whether the encoded data should start with a byte value of 2
 * @param init Initializer function for setting up this struct
 * @receiver
 * @return The newly created struct
 */
inline fun group(label: String = "", start2: Boolean = false, init: ContentInitializer): GroupTdf {
    val context = TdfBuilder()
    context.init()
    return GroupTdf(label, start2, context.values)
}

