package com.jacobtread.blaze.tdf

import io.netty.buffer.ByteBuf

class VarIntListTdf(label: String, override val value: List<ULong>) : Tdf<List<ULong>>(label, INT_LIST) {
    companion object : TdfReadable<VarIntListTdf> {
        override fun read(label: String, input: ByteBuf): VarIntListTdf {
            val count = readVarInt(input).toInt()
            val values = ArrayList<ULong>(count)
            repeat(count) { values.add(readVarInt(input)) }
            return VarIntListTdf(label, values)
        }
    }

    override fun write(out: ByteBuf) {
        writeVarInt(out, value.size.toULong())
        if (value.isNotEmpty()) {
            value.forEach { writeVarInt(out, it) }
        }
    }

    override fun computeSize(): Int {
        var size = computeVarIntSize(value.size.toULong())
        if (value.isNotEmpty()) {
            value.forEach { size += computeVarIntSize(it) }
        }
        return size
    }

    override fun toString(): String = "VarIntList($label: $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VarIntListTdf) return false
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