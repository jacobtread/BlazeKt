package com.jacobtread.blaze.handler

import com.jacobtread.blaze.logging.PacketLogger
import com.jacobtread.blaze.packet.LazyBufferPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Handler for decoding incoming [ByteBuf]'s into packets.
 *
 * @constructor Creates a new packet decoder
 */
class PacketDecoder : ChannelInboundHandlerAdapter() {

    /**
     * The buffer which input bytes are all merged into in order
     * to create the entire buffer required for the packet to be
     * read
     */
    private var inputBuffer: ByteBuf = Unpooled.EMPTY_BUFFER

    /**
     * Whether a reading of packets was attempted by [decodePacket]
     * determines whether more data should be read inside
     * [channelReadComplete]
     */
    private var readAttempted = false

    /**
     * Whether any packets were managed to be read by the
     * [decodePacket] function.
     */
    private var readSuccess: Boolean = false

    /**
     * The number of read's attempted against the current
     * [inputBuffer]. After reaching 16 reads the read bytes
     * are discarded for [inputBuffer]
     */
    private var readCount: Int = 0

    /**
     * Handles reading and merging byte buffers in order to
     * read packets from them. Ignores the messages that
     * aren't [ByteBuf]'s
     *
     * @param ctx The channel handler context
     * @param msg The message that was read
     */
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            try {
                val alloc = ctx.alloc()

                // Grow the existing buffer to fit the new bytes
                growBuffer(alloc, msg)
                // Try and decode packets from the bytes
                decodePacket(alloc, ctx)
            } finally {
                if (!inputBuffer.isReadable) {
                    readCount = 0
                    inputBuffer.release()
                    inputBuffer = Unpooled.EMPTY_BUFFER
                } else {
                    readCount++
                    if (readCount >= 16) {
                        discardReadBytes()
                    }
                }
            }
        } else {
            // Pass it down the pipeline
            ctx.fireChannelRead(msg)
        }
    }

    /**
     * Handles automatic reading if no packets were read
     * from the current input buffer. Also discards bytes
     * that were already read.
     *
     * @param ctx The channel handler context
     */
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        discardReadBytes()
        val isAutoRead = ctx.channel()
            .config()
            .isAutoRead
        // If auto read is enabled, and we attempted read but didn't get any packets
        if (readAttempted && !readSuccess && isAutoRead) {
            ctx.read() // Try and read again
        }
        ctx.fireChannelReadComplete()
    }

    /**
     * Attempts to decode a packet from the input buffer.
     * Will set [readSuccess] to true if any packets got
     * read. Will reset the buffer index back to before
     * the packet length if a read doesn't have enough
     * bytes to complete.
     *
     * @param alloc The allocator for allocating byte buffers
     * @param ctx The channel handler context
     */
    private fun decodePacket(alloc: ByteBufAllocator, ctx: ChannelHandlerContext) {
        val inputBuffer = inputBuffer
        readAttempted = true
        readSuccess = false
        // Ensure there's at least two bytes for the length
        while (inputBuffer.readableBytes() >= 2) {
            val startIndex = inputBuffer.readerIndex()

            val length = inputBuffer.readUnsignedShort() // 2

            // Ensure we have 10 bytes for the heading information
            if (inputBuffer.readableBytes() < 10) {
                inputBuffer.readerIndex(startIndex)
                break
            }

            val component = inputBuffer.readUnsignedShort() // 4
            val command = inputBuffer.readUnsignedShort() // 6
            val error = inputBuffer.readUnsignedShort() // 8
            val qtype = inputBuffer.readUnsignedShort() // 10
            val id = inputBuffer.readUnsignedShort() // 12

            val extLength: Int = if ((qtype and 0x10) != 0) {
                // Ensure there's two bytes for the ext length
                if (inputBuffer.readableBytes() < 2) {
                    inputBuffer.readerIndex(startIndex)
                    break
                }
                inputBuffer.readUnsignedShort()
            } else {
                0
            }

            val contentLength = length + (extLength shl 16)

            // Ensure there's enough bytes for the whole content
            if (inputBuffer.readableBytes() < contentLength) {
                inputBuffer.readerIndex(startIndex)
                break
            }

            val content = alloc.buffer(contentLength, contentLength)
            inputBuffer.readBytes(content, contentLength)// Read the bytes into a new buffer and use that as content
            val packet = LazyBufferPacket(component, command, error, qtype, id, content)

            if (PacketLogger.isEnabled) {
                PacketLogger.log("DECODED PACKET", ctx.channel(), packet)
            }
            ctx.fireChannelRead(packet)
            readSuccess = true
        }
    }

    /**
     * Discards any bytes from [inputBuffer] that have already been
     * read and also sets the [readCount] to zero
     */
    private fun discardReadBytes() {
        readCount = 0
        if (inputBuffer.refCnt() == 1) {
            inputBuffer.discardReadBytes()
        }
    }

    /**
     * Grows the input buffer in order to fit the
     * provided [msg].
     *
     * @param alloc The byte buf allocator
     * @param msg The message to grow for
     */
    private fun growBuffer(alloc: ByteBufAllocator, msg: ByteBuf) {
        if (!inputBuffer.isReadable && inputBuffer.isContiguous) {
            inputBuffer.release()
            inputBuffer = msg
            return
        }

        try {
            val requiredSpace = msg.readableBytes()
            if (requiredSpace > inputBuffer.maxWritableBytes()
                || requiredSpace > inputBuffer.maxFastWritableBytes() && inputBuffer.refCnt() > 1
                || inputBuffer.isReadOnly
            ) {
                val oldBufferSize = inputBuffer.readableBytes()

                val newBufferSize = oldBufferSize + requiredSpace
                val newBuffer = alloc.buffer(alloc.calculateNewCapacity(newBufferSize, Int.MAX_VALUE))

                var toRelease = newBuffer // Release the new buffer on failure
                try {
                    newBuffer
                        .setBytes(0, inputBuffer, inputBuffer.readerIndex(), oldBufferSize)
                        .setBytes(oldBufferSize, msg, msg.readerIndex(), requiredSpace)
                        .writerIndex(newBufferSize)
                    msg.readerIndex(msg.writerIndex())
                    toRelease = inputBuffer // Release the old buffer instead
                    inputBuffer = newBuffer
                    return
                } finally {
                    toRelease.release()
                }
            }
            inputBuffer.writeBytes(msg, msg.readerIndex(), requiredSpace)
            msg.readerIndex(msg.writerIndex())
        } finally {
            msg.release()
        }
    }
}