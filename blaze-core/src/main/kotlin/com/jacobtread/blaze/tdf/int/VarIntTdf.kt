package com.jacobtread.blaze.tdf.int

import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.TdfReadable
import io.netty.buffer.ByteBuf

sealed class VarIntTdf<T>(label: String) : Tdf<T>(label, VARINT) {

    companion object : TdfReadable<VarIntTdf<*>> {
        override fun read(label: String, input: ByteBuf): VarIntTdf<*> {
            val value = readVarInt(input)
            if (value <= 127u) {
                return ByteTdf(label, (value and 0xFFu).toByte())
            } else if (value <= 255u) {
                return UByteTdf(label, value.toUByte())
            } else if (value <= 32767u) {
                return ShortTdf(label, (value and 0xFFFFu).toShort())
            }
            return ULongTdf(label, readVarInt(input))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VarIntTdf<*>) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    abstract fun toByte(): Byte
    abstract fun toInt(): Int
    abstract fun toShort(): Short
    abstract fun toLong(): Long

    abstract fun toUByte(): UByte
    abstract fun toUInt(): UInt
    abstract fun toUShort(): UShort
    abstract fun toULong(): ULong

}