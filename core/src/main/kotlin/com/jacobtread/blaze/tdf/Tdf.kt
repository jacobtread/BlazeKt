package com.jacobtread.blaze.tdf

import com.jacobtread.blaze.TdfReadException
import com.jacobtread.blaze.data.VarPair
import com.jacobtread.blaze.data.VarTripple
import io.netty.buffer.ByteBuf

abstract class Tdf<V>(val label: String, private val tagType: UByte) {
    companion object {
        const val VARINT: UByte = 0x0u
        const val STRING: UByte = 0x1u
        const val BLOB: UByte = 0x02u
        const val GROUP: UByte = 0x3u
        const val LIST: UByte = 0x4u
        const val MAP: UByte = 0x5u
        const val OPTIONAL: UByte = 0x6u
        const val INT_LIST: UByte = 0x7u
        const val PAIR: UByte = 0x8u
        const val TRIPPLE: UByte = 0x9u
        const val FLOAT: UByte = 0xAu

        fun getTypeFromClass(valueType: Class<*>): UByte {
            return when (valueType) {
                java.lang.Long::class.java,
                java.lang.Integer::class.java,
                ULong::class.java,
                UInt::class.java,
                -> VARINT
                String::class.java -> STRING
                Float::class.java -> FLOAT
                GroupTdf::class.java -> GROUP
                VarTripple::class.java -> TRIPPLE
                else -> throw IllegalArgumentException("Don't know how to handle type \"${valueType.simpleName}\"")
            }
        }

        fun createTag(labelIn: String): Long {
            val res = IntArray(3)
            val buff = IntArray(4)
            for (i in 0 until 4) {
                if (i >= labelIn.length) {
                    buff[i] = 0
                } else {
                    buff[i] = labelIn[i].code
                }
            }
            res[0] = res[0].or(buff[0].and(0x40).shl(1))
            res[0] = res[0].or(buff[0].and(0x10).shl(2))
            res[0] = res[0].or(buff[0].and(0x0F).shl(2))
            res[0] = res[0].or(buff[1].and(0x40).shr(5))
            res[0] = res[0].or(buff[1].and(0x10).shr(4))

            res[1] = res[1].or(buff[1].and(0x0F).shl(4))
            res[1] = res[1].or(buff[2].and(0x40).shr(3))
            res[1] = res[1].or(buff[2].and(0x10).shr(2))
            res[1] = res[1].or(buff[2].and(0x0C).shr(2))

            res[2] = res[2].or(buff[2].and(0x03).shl(6))
            res[2] = res[2].or(buff[3].and(0x40).shr(1))
            res[2] = res[2].or(buff[3].and(0x1F))

            var tag = 0L
            tag = tag.or(res[0].shl(24).toLong())
            tag = tag.or(res[1].shl(16).toLong())
            tag = tag.or(res[2].shl(8).toLong())
            return tag
        }

        fun createLabel(tag: Long): String {
            val buff = byteArrayOf(
                (tag shr 24).toByte(),
                (tag shr 16).toByte(),
                (tag shr 8).toByte(),
            )
            val res = IntArray(4)
            res[0] = res[0] or (buff[0].toInt() and 0x80 shr 1)
            res[0] = res[0] or (buff[0].toInt() and 0x40 shr 2)
            res[0] = res[0] or (buff[0].toInt() and 0x30 shr 2)
            res[0] = res[0] or (buff[0].toInt() and 0x0C shr 2)

            res[1] = res[1] or (buff[0].toInt() and 0x02 shl 5)
            res[1] = res[1] or (buff[0].toInt() and 0x01 shl 4)
            res[1] = res[1] or (buff[1].toInt() and 0xF0 shr 4)

            res[2] = res[2] or (buff[1].toInt() and 0x08 shl 3)
            res[2] = res[2] or (buff[1].toInt() and 0x04 shl 2)
            res[2] = res[2] or (buff[1].toInt() and 0x03 shl 2)
            res[2] = res[2] or (buff[2].toInt() and 0xC0 shr 6)
            res[3] = res[3] or (buff[2].toInt() and 0x20 shl 1)
            res[3] = res[3] or (buff[2].toInt() and 0x1F)
            val output = StringBuilder()
            for (i in 0..3) {
                if (res[i] != 0) output.append(res[i].toChar())
            }
            return output.toString()
        }

        fun read(input: ByteBuf): Tdf<*> {
            val head = input.readUnsignedInt()
            val tag = (head and 0xFFFFFF00)
            val label = createLabel(tag)
            val type: UByte = (head and 0xFF).toUByte()
            try {
                return when (type) {
                    VARINT -> VarIntTdf.read(label, input)
                    STRING -> StringTdf.read(label, input)
                    BLOB -> BlobTdf.read(label, input)
                    GROUP -> GroupTdf.read(label, input)
                    LIST -> ListTdf.read(label, input)
                    MAP -> MapTdf.read(label, input)
                    OPTIONAL -> OptionalTdf.read(label, input)
                    INT_LIST -> VarIntListTdf.read(label, input)
                    PAIR -> PairTdf.read(label, input)
                    TRIPPLE -> TrippleTdf.read(label, input)
                    FLOAT -> FloatTdf.read(label, input)
                    else -> throw IllegalStateException("Unknown Tdf type: $type")
                }
            } catch (e: Throwable) {
                throw TdfReadException(label, type, e)
            }
        }

        fun computeVarIntSizeFuzzy(value: Any?): Int {
            if (value == null) {
                return 1
            }

            return when (value) {
                is ULong -> computeVarIntSize(value)
                is Long -> computeVarIntSize(value.toULong())
                is Int -> computeVarIntSize(value.toULong())
                is UInt -> computeVarIntSize(value.toULong())
                is Number -> computeVarIntSize(value.toLong().toULong())
                else -> 1
            }
        }

        fun computeVarIntSize(value: ULong): Int {
            return if (value < 64u) {
                1
            } else {
                var size = 1
                var curShift = value shr 6
                while (curShift >= 128u) {
                    curShift = curShift shr 7
                    size++
                }
                size + 1
            }
        }

        fun computeStringSize(value: String): Int {
            val v = if (value.endsWith(Char.MIN_VALUE)) value else (value + '\u0000')
            val bytes = v.toByteArray(Charsets.UTF_8)
            return computeVarIntSize(bytes.size.toULong()) + bytes.size
        }

        fun computeVarTrippleSize(value: VarTripple): Int {
            return computeVarIntSize(value.a) + computeVarIntSize(value.b) + computeVarIntSize(value.c)
        }

        fun computeVarPairSize(value: VarPair): Int {
            return computeVarIntSize(value.a) + computeVarIntSize(value.b)
        }

        fun writeVarIntFuzzy(buffer: ByteBuf, value: Any?) {
            if (value == null) {
                buffer.writeByte(0)
                return
            }

            when (value) {
                is ULong -> writeVarInt(buffer, value)
                is Long -> writeVarInt(buffer, value.toULong())
                is Int -> writeVarInt(buffer, value.toULong())
                is UInt -> writeVarInt(buffer, value.toULong())
                is Number -> writeVarInt(buffer, value.toLong().toULong())
                else -> throw RuntimeException("Tried to write varint of unknown type: \"$value\" (${value.javaClass.simpleName})")
            }
        }

        fun writeVarInt(buffer: ByteBuf, value: ULong) {
            if (value < 64u) {
                buffer.writeByte((value and 255u).toInt())
            } else {
                var curByte = (value and 63u).toUByte() or 0x80u
                buffer.writeByte(curByte.toInt())
                var curShift = value shr 6
                while (curShift >= 128u) {
                    curByte = ((curShift and 127u) or 128u).toUByte()
                    curShift = curShift shr 7
                    buffer.writeByte(curByte.toInt())
                }
                buffer.writeByte(curShift.toInt())
            }
        }

        fun writeVarTripple(buffer: ByteBuf, value: VarTripple) {
            writeVarInt(buffer, value.a)
            writeVarInt(buffer, value.b)
            writeVarInt(buffer, value.c)
        }

        fun writeVarPair(buffer: ByteBuf, value: VarPair) {
            writeVarInt(buffer, value.a)
            writeVarInt(buffer, value.b)
        }

        fun writeString(buffer: ByteBuf, value: String) {
            val v = if (value.endsWith(Char.MIN_VALUE)) value else (value + '\u0000')
            val bytes = v.toByteArray(Charsets.UTF_8)
            writeVarInt(buffer, bytes.size.toULong())
            buffer.writeBytes(bytes)
        }

        fun readUnsignedByte(buffer: ByteBuf): UByte {
            return buffer.readUnsignedByte().toUByte()
        }

        fun readVarInt(buffer: ByteBuf): ULong {
            val firstByte = buffer.readUnsignedByte().toUByte()
            var result: ULong = (firstByte and 63u).toULong()
            if (firstByte < 128u) return result
            var shift = 6
            var byte: UByte
            do {
                byte = buffer.readUnsignedByte().toUByte()
                result = result or ((byte and 127u).toULong() shl shift)
                shift += 7
            } while (byte >= 128u)
            return result
        }

        fun readVarTripple(buffer: ByteBuf): VarTripple {
            return VarTripple(
                readVarInt(buffer),
                readVarInt(buffer),
                readVarInt(buffer),
            )
        }

        fun readVarPair(buffer: ByteBuf): VarPair {
            return VarPair(
                readVarInt(buffer),
                readVarInt(buffer),
            )
        }

        fun readString(buffer: ByteBuf): String {
            val length = readVarInt(buffer)
            val bytes = ByteArray(length.toInt() - 1)
            buffer.readBytes(bytes)
            buffer.readUnsignedByte()
            return String(bytes, Charsets.UTF_8)
        }
    }

    abstract val value: V

    abstract fun write(out: ByteBuf)

    abstract fun computeSize(): Int

    fun computeFullSize(): Int {
        return 4 + computeSize()
    }


    fun writeFully(out: ByteBuf) {
        val tag = createTag(label)
        out.writeByte(tag.shr(24).and(0xFF).toInt())
        out.writeByte(tag.shr(16).and(0xFF).toInt())
        out.writeByte(tag.shr(8).and(0xFF).toInt())
        out.writeByte(tagType.toInt())
        write(out)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tdf<*>) return false
        if (label != other.label) return false
        if (tagType != other.tagType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + tagType.toInt()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}

