package com.jacobtread.blaze.tdf

interface VarIntConversion {
    fun toInt(): Int
    fun toUInt(): UInt
    fun toShort(): Short
    fun toUShort(): UShort
    fun toByte(): Byte
    fun toUByte(): UByte
    fun toULong(): ULong
}