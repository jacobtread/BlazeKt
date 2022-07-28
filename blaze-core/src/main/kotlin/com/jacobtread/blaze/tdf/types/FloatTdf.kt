package com.jacobtread.blaze.tdf.types

import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.TdfReadable
import io.netty.buffer.ByteBuf

class FloatTdf(label: String, override val value: Float) : Tdf<Float>(label, FLOAT) {
    companion object : TdfReadable<FloatTdf> {
        override fun read(label: String, input: ByteBuf): FloatTdf {
            val value = input.readFloat()
            return FloatTdf(label, value)
        }
    }

    override fun write(out: ByteBuf) {
        out.writeFloat(value)
    }

    override fun computeSize(): Int = 4
    override fun toString(): String = "Float($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatTdf) return false
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