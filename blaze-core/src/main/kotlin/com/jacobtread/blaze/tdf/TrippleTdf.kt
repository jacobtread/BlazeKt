package com.jacobtread.blaze.tdf

import com.jacobtread.blaze.data.VarTriple
import io.netty.buffer.ByteBuf

class TrippleTdf(label: String, override val value: VarTriple) : Tdf<VarTriple>(label, TRIPPLE) {
    companion object : TdfReadable<TrippleTdf> {
        override fun read(label: String, input: ByteBuf): TrippleTdf = TrippleTdf(label, readVarTripple(input))
    }

    override fun write(out: ByteBuf) = writeVarTripple(out, value)
    override fun computeSize(): Int = computeVarTrippleSize(value)
    override fun toString(): String = "Tripple($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrippleTdf) return false
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