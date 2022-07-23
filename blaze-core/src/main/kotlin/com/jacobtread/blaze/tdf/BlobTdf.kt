package com.jacobtread.blaze.tdf

import io.netty.buffer.ByteBuf

class BlobTdf(label: String, override val value: ByteArray) : Tdf<ByteArray>(label, BLOB) {
    companion object : TdfReadable<BlobTdf> {
        override fun read(label: String, input: ByteBuf): BlobTdf {
            val size = readVarInt(input).toInt()
            val byteArray = ByteArray(size)
            if (size > 0) input.readBytes(byteArray)
            return BlobTdf(label, byteArray)
        }
    }

    override fun write(out: ByteBuf) {
        writeVarInt(out, value.size.toULong())
        if (value.isNotEmpty()) {
            out.writeBytes(value)
        }
    }

    override fun computeSize(): Int {
        val size = computeVarIntSize(value.size.toULong())
        return if (value.isNotEmpty()) {
            size + value.size
        } else {
            size
        }
    }

    override fun toString(): String = "Blob($label: ${value.contentToString()})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlobTdf) return false
        if (!super.equals(other)) return false
        if (!value.contentEquals(other.value)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}