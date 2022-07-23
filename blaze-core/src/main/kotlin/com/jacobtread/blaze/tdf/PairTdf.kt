package com.jacobtread.blaze.tdf

import com.jacobtread.blaze.data.VarPair
import io.netty.buffer.ByteBuf

class PairTdf(label: String, override val value: VarPair) : Tdf<VarPair>(label, PAIR) {
    companion object : TdfReadable<PairTdf> {
        override fun read(label: String, input: ByteBuf): PairTdf = PairTdf(label, readVarPair(input))
    }

    override fun write(out: ByteBuf) = writeVarPair(out, value)
    override fun computeSize(): Int = computeVarPairSize(value)
    override fun toString(): String = "Pair($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PairTdf) return false
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