@file:Suppress("NOTHING_TO_INLINE")

package com.jacobtread.blaze

import com.jacobtread.blaze.data.VarPair
import com.jacobtread.blaze.data.VarTriple
import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.types.*

/**
 * TdfContainer Structure representing a collection of TDFs that can be queried for
 * different value types. This has lots of inline helper shortcut functions for easily
 * finding different data types
 *
 * @constructor Create empty TdfContainer
 */
interface TdfContainer {

    /**
     * getTdfByLabel This is the only function the underlying implementations
     * need to implement this allows searching for TDFs by a provided label
     *
     * @param label The label to search for
     * @return The found TDF or null
     */
    fun getTdfByLabel(label: String): Tdf<*>?

    /**
     * getTdf Retrieves a TDF with the matching type and label
     *
     * @param C The tdf generic type
     * @param type The class of the type of the TDF
     * @param label The label of the tdf to search for
     * @throws MissingTdfException Thrown when there was no TDFs with the provided label
     * @throws InvalidTdfException Thrown when the TDF was not of the provided type
     * @return The TDF that was found
     */
    @Throws(MissingTdfException::class, InvalidTdfException::class)
    fun <C : Tdf<*>> getTdf(type: Class<C>, label: String): C {
        val value = getTdfByLabel(label) ?: throw MissingTdfException(label)
        if (!value.javaClass.isAssignableFrom(type)) throw InvalidTdfException(label, type, value.javaClass)
        return type.cast(value)
    }

    /**
     * getTdfOrNull Retrieves a TDF with the matching type and label
     * or null if either there was none with the matching label or the
     * type of the TDF doesn't assign from C
     *
     * @param C The tdf generic type
     * @param type The class of the type of the TDF
     * @param label The label of the tdf to search for
     * @return The TDF that was found or null if it was missing or invalid
     */
    fun <C : Tdf<*>> getTdfOrNull(type: Class<C>, label: String): C? {
        val value = getTdfByLabel(label)
        if (value == null || !value.javaClass.isAssignableFrom(type)) return null
        return type.cast(value)
    }

    /**
     * getValue Retrieves the value of a TDF with the matching type and label
     *
     * @param T The data type that the TDF value will be
     * @param C The TDF generic type
     * @param type The class of the type of the TDF
     * @param label The label to search for
     * @throws MissingTdfException Thrown when there was no TDFs with the provided label
     * @throws InvalidTdfException Thrown when the TDF was not of the provided type
     * @return The value of the TDF that was found
     */
    @Throws(MissingTdfException::class, InvalidTdfException::class)
    fun <T, C : Tdf<T>> getValue(type: Class<C>, label: String): T {
        val value = getTdfByLabel(label) ?: throw MissingTdfException(label)
        if (!value.javaClass.isAssignableFrom(type)) throw InvalidTdfException(label, type, value.javaClass)
        return type.cast(value).value

    }

    /**
     * getValueOrNull Retrieves the value of a TDF with the matching type and label or
     * null if there are no TDFs with that label or if the type is not assignable
     *
     * @param T The data type that the TDF value will be
     * @param C The TDF generic type
     * @param type The class of the type of the TDF
     * @param label The label to search for
     * @return
     */
    fun <T, C : Tdf<T>> getValueOrNull(type: Class<C>, label: String): T? {
        val value = getTdfByLabel(label)
        if (value == null || !value.javaClass.isAssignableFrom(type)) return null
        return type.cast(value).value
    }
}

//region Helper Functions

// Tdf Struct-Like Helpers

inline fun TdfContainer.varInt(label: String): VarIntTdf<*> = getTdf(VarIntTdf::class.java, label)
inline fun TdfContainer.group(label: String): GroupTdf = getTdf(GroupTdf::class.java, label)
inline fun TdfContainer.optional(label: String): OptionalTdf = getTdf(OptionalTdf::class.java, label)

inline fun TdfContainer.varIntOrNull(label: String): VarIntTdf<*>? = getTdfOrNull(VarIntTdf::class.java, label)
inline fun TdfContainer.groupOrNull(label: String): GroupTdf? = getTdfOrNull(GroupTdf::class.java, label)
inline fun TdfContainer.optionalOrNull(label: String): OptionalTdf? = getTdfOrNull(OptionalTdf::class.java, label)

// Non-nullable Helpers

inline fun TdfContainer.text(label: String): String = getValue(StringTdf::class.java, label)


inline fun TdfContainer.number(label: String): ULong = varInt(label).toULong()
inline fun TdfContainer.numberLong(label: String): Long = number(label).toLong()
inline fun TdfContainer.numberInt(label: String): Int = varInt(label).toInt()
inline fun TdfContainer.numberUInt(label: String): UInt = varInt(label).toUInt()
inline fun TdfContainer.numberShort(label: String): Short = varInt(label).toShort()
inline fun TdfContainer.numberUShort(label: String): UShort = varInt(label).toUShort()

inline fun TdfContainer.float(label: String): Float = getValue(FloatTdf::class.java, label)
inline fun TdfContainer.blob(label: String): ByteArray = getValue(BlobTdf::class.java, label)
inline fun TdfContainer.unionValue(label: String): Tdf<*>? = getValue(OptionalTdf::class.java, label)
inline fun TdfContainer.triple(label: String): VarTriple = getValue(TrippleTdf::class.java, label)
inline fun TdfContainer.pair(label: String): VarPair = getValue(PairTdf::class.java, label)
inline fun TdfContainer.varIntList(label: String): List<ULong> = getValue(VarIntListTdf::class.java, label)

@Suppress("UNCHECKED_CAST")
inline fun <V : Any> TdfContainer.list(label: String): List<V> = getValue(ListTdf::class.java, label) as List<V>

@Suppress("UNCHECKED_CAST")
inline fun <K : Any, V : Any> TdfContainer.map(label: String): Map<K, V> = getValue(MapTdf::class.java, label) as Map<K, V>

// Nullable Helpers

inline fun TdfContainer.textOrNull(label: String): String? = getValueOrNull(StringTdf::class.java, label)
inline fun TdfContainer.numberOrNull(label: String): ULong? = varIntOrNull(label)?.toULong()
inline fun TdfContainer.numberLongOrNull(label: String): Long? = numberOrNull(label)?.toLong()
inline fun TdfContainer.numberIntOrNull(label: String): Int? = varIntOrNull(label)?.toInt()
inline fun TdfContainer.numberUIntOrNull(label: String): UInt? = varIntOrNull(label)?.toUInt()
inline fun TdfContainer.numberShortOrNull(label: String): Short? = varIntOrNull(label)?.toShort()
inline fun TdfContainer.numberUShortOrNull(label: String): UShort? = varIntOrNull(label)?.toUShort()

inline fun TdfContainer.floatOrNull(label: String): Float? = getValueOrNull(FloatTdf::class.java, label)
inline fun TdfContainer.blobOrNull(label: String): ByteArray? = getValueOrNull(BlobTdf::class.java, label)
inline fun TdfContainer.unionValueOrNull(label: String): Tdf<*>? = getValueOrNull(OptionalTdf::class.java, label)
inline fun TdfContainer.tripleOrNull(label: String): VarTriple? = getValueOrNull(TrippleTdf::class.java, label)
inline fun TdfContainer.pairOrNull(label: String): VarPair? = getValueOrNull(PairTdf::class.java, label)
inline fun TdfContainer.varIntListOrNull(label: String): List<ULong>? = getValueOrNull(VarIntListTdf::class.java, label)


@Suppress("UNCHECKED_CAST")
inline fun <V : Any> TdfContainer.listOrNull(label: String): List<V>? = getValueOrNull(ListTdf::class.java, label) as List<V>?

@Suppress("UNCHECKED_CAST")
inline fun <K : Any, V : Any> TdfContainer.mapOrNull(label: String): Map<K, V>? = getValueOrNull(MapTdf::class.java, label) as Map<K, V>?
//endregion