package com.jacobtread.blaze.tdf.types

import io.netty.buffer.ByteBuf

class ByteTdf(label: String, override val value: Byte) : VarIntTdf<Byte>(label) {

    override fun write(out: ByteBuf) {
        val value = value.toInt()
        if (value < 0x40) {
            out.writeByte((value and 0xFF))
        } else {
            out.writeByte((value and 0x3f) or 0x80)
            out.writeByte(value shr 6)
        }
    }

    override fun computeSize(): Int {
        val value = value.toInt()
        return if (value < 0x40) 1 else 2
    }

    override fun toByte(): Byte = value
    override fun toShort(): Short = value.toShort()
    override fun toUByte(): UByte = value.toUByte()
    override fun toULong(): ULong = value.toULong()
    override fun toUInt(): UInt = value.toUInt()
    override fun toInt(): Int = value.toInt()
    override fun toUShort(): UShort = value.toUShort()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteTdf) return false
        if (!super.equals(other)) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}