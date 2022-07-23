package com.jacobtread.blaze.tdf

import io.netty.buffer.ByteBuf

class VarIntTdf(label: String, override val value: ULong) : Tdf<ULong>(label, VARINT) {
    companion object : TdfReadable<VarIntTdf> {
        override fun read(label: String, input: ByteBuf): VarIntTdf = VarIntTdf(label, readVarInt(input))
    }

    override fun write(out: ByteBuf) = writeVarInt(out, value)
    override fun computeSize(): Int = computeVarIntSize(value)
    override fun toString(): String = "VarInt($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VarIntTdf) return false
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