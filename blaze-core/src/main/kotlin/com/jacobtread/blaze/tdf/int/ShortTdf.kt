package com.jacobtread.blaze.tdf.int

import io.netty.buffer.ByteBuf

class ShortTdf(label: String, override val value: Short) : VarIntTdf<Short>(label) {
    override fun write(out: ByteBuf) {
        val value = value.toInt()
        if (value < 0x40) {
            out.writeByte((value and 255) and 0xFF)
        } else {
            out.writeByte((value and 0x3f) or 0x80)
            var shift = value shr 6
            if (shift >= 0x80) {
                out.writeByte(((shift and 127) or 128) and 0xFF)
                shift = shift shr 7
            }
            out.writeByte(shift)
        }
    }

    override fun computeSize(): Int {
        val value = value.toInt()
        if (value < 0x40) return 1
        val shift = value shr 6
        if (shift < 0x80) return 2
        return 3
    }

    override fun toByte(): Byte = value.toByte()
    override fun toInt(): Int = value.toInt()
    override fun toShort(): Short = value
    override fun toLong(): Long = value.toLong()
    override fun toUByte(): UByte = value.toUByte()
    override fun toUInt(): UInt = value.toUInt()
    override fun toUShort(): UShort = value.toUShort()
    override fun toULong(): ULong = value.toULong()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShortTdf) return false
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