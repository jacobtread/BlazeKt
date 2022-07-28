package com.jacobtread.blaze.tdf.types

import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.TdfReadable
import io.netty.buffer.ByteBuf

class MapTdf(
    label: String,
    private val keyType: UByte,
    private val valueType: UByte,
    override val value: Map<*, *>,
) : Tdf<Map<*, *>>(label, MAP) {

    companion object : TdfReadable<MapTdf> {
        override fun read(label: String, input: ByteBuf): MapTdf {
            val keyType = readUnsignedByte(input)
            val valueType = readUnsignedByte(input)
            val count = readVarInt(input).toInt()
            val out = LinkedHashMap<Any, Any>()
            repeat(count) {
                val key: Any = when (keyType) {
                    VARINT -> readVarInt(input)
                    STRING -> readString(input)
                    else -> throw IllegalStateException("Unknown list subtype $keyType")
                }
                val value: Any = when (valueType) {
                    VARINT -> readVarInt(input)
                    STRING -> readString(input)
                    GROUP -> GroupTdf.read("", input)
                    FLOAT -> input.readFloat()
                    else -> throw IllegalStateException("Unknown list subtype $keyType")
                }
                out[key] = value
            }
            return MapTdf(label, keyType, valueType, out)
        }
    }

    override fun write(out: ByteBuf) {
        out.writeByte(keyType.toInt())
        out.writeByte(valueType.toInt())
        val entries = value.entries
        writeVarInt(out, entries.size.toULong())
        for ((key, value) in entries) {
            writeType(out, keyType, key)
            writeType(out, valueType, value)
        }
    }

    private fun writeType(out: ByteBuf, type: UByte, value: Any?) {
        when (type) {
            VARINT -> writeVarIntFuzzy(out, value)
            STRING -> writeString(out, value as String)
            GROUP -> (value as GroupTdf).write(out)
            FLOAT -> out.writeFloat(value as Float)
        }
    }

    override fun computeSize(): Int {
        val entries = value.entries
        var size = 2 + computeVarIntSize(entries.size.toULong())
        for ((key, value) in entries) {
            size += computeTypeSize(keyType, key) + computeTypeSize(valueType, value)
        }
        return size
    }

    private fun computeTypeSize(type: UByte, value: Any?): Int {
        return when (type) {
            VARINT -> computeVarIntSizeFuzzy(value)
            STRING -> computeStringSize(value as String)
            GROUP -> (value as GroupTdf).computeSize()
            FLOAT -> 4
            else -> throw IllegalStateException("Unknown list subtype $keyType")
        }
    }

    override fun toString(): String = "Map($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapTdf) return false
        if (!super.equals(other)) return false
        if (keyType != other.keyType) return false
        if (valueType != other.valueType) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + keyType.toInt()
        result = 31 * result + valueType.toInt()
        result = 31 * result + value.hashCode()
        return result
    }
}