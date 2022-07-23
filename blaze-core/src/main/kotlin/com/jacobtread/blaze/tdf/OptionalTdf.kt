package com.jacobtread.blaze.tdf

import io.netty.buffer.ByteBuf

class OptionalTdf(label: String, val type: UByte = NO_VALUE_TYPE, override val value: Tdf<*>? = null) : Tdf<Tdf<*>?>(label, OPTIONAL) {
    companion object {
        const val NO_VALUE_TYPE: UByte = 0x7Fu

        fun read(label: String, input: ByteBuf): OptionalTdf {
            val type = readUnsignedByte(input)
            val value = if (type != NO_VALUE_TYPE) {
                read(input)
            } else null
            return OptionalTdf(label, type, value)
        }
    }

    fun hasValue(): Boolean = type != NO_VALUE_TYPE && value != null

    override fun write(out: ByteBuf) {
        out.writeByte(type.toInt())
        if (hasValue()) {
            value?.writeFully(out)
        }
    }

    override fun computeSize(): Int {
        return if (hasValue()) {
            value!!.computeFullSize() + 1
        } else {
            1
        }
    }

    override fun toString(): String = "Optional($label: $type, $value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OptionalTdf) return false
        if (!super.equals(other)) return false
        if (type != other.type) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.toInt()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}