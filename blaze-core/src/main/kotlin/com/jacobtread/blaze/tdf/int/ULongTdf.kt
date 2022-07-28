package com.jacobtread.blaze.tdf.int

import io.netty.buffer.ByteBuf

class ULongTdf(label: String, override val value: ULong) : VarIntTdf<ULong>(label) {

    constructor(label: String, value: Int) : this(label, value.toULong())
    constructor(label: String, value: UShort) : this(label, value.toULong())

    override fun write(out: ByteBuf) {
        return writeVarInt(out, value)
    }

    override fun computeSize(): Int = computeVarIntSize(value)
    override fun toByte(): Byte = value.toByte()
    override fun toInt(): Int = value.toInt()
    override fun toShort(): Short = value.toShort()
    override fun toLong(): Long = value.toLong()
    override fun toUByte(): UByte = value.toUByte()
    override fun toUInt(): UInt = value.toUInt()
    override fun toUShort(): UShort = value.toUShort()
    override fun toULong(): ULong = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ULongTdf) return false
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