package com.jacobtread.blaze.tdf.types

import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.TdfReadable
import com.jacobtread.blaze.tdf.VarIntConversion
import io.netty.buffer.ByteBuf

sealed class VarIntTdf<T>(label: String) : Tdf<T>(label, VARINT), VarIntConversion {

    companion object : TdfReadable<VarIntTdf<*>> {
        override fun read(label: String, input: ByteBuf): VarIntTdf<*> {
            val value = readVarInt(input)
            return if (value <= 127u) {
                ByteTdf(label, (value and 0xFFu).toByte())
            } else if (value <= 255u) {
                UByteTdf(label, value.toUByte())
            } else if (value <= 32767u) {
                ShortTdf(label, (value and 0xFFFFu).toShort())
            } else {
                ULongTdf(label, value)
            }
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

}