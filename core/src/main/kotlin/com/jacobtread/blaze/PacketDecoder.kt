package com.jacobtread.blaze

import com.jacobtread.blaze.packet.LazyBufferPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.DecoderException

class PacketDecoder : ChannelInboundHandlerAdapter() {

    private var cumulation: ByteBuf? = null
    private var first = false
    private var numReads = 0
    private var firedChannelRead = false
    private var selfFiredChannelRead = false


    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is ByteBuf) {
            ctx.fireChannelRead(msg)
            return
        }
        selfFiredChannelRead = true
        try {
            first = cumulation == null
            val cumulation = mergeCumulate(ctx.alloc(), if (first) Unpooled.EMPTY_BUFFER else cumulation!!, msg)
            this.cumulation = cumulation
            if (decode(ctx, cumulation)) {
                firedChannelRead = true
            }
        } catch (e: DecoderException) {
            throw e
        } catch (e: Exception) {
            throw DecoderException(e)
        } finally {
            val cumulation = cumulation
            if (cumulation != null && !cumulation.isReadable) {
                numReads = 0
                cumulation.release()
                this.cumulation = null
            } else if (++numReads >= 16) {
                numReads = 0
                discardSomeReadBytes()
            }
        }
    }

    /**
     * Decode
     *
     * @param ctx
     * @param input
     * @return Returns false if the packet was not fully read yet
     */
    private fun decode(ctx: ChannelHandlerContext, input: ByteBuf): Boolean {
        var read = false
        while (input.readableBytes() >= 2) {
            val startIndex = input.readerIndex()

            // ushort = 2 indexes

            val length = input.readUnsignedShort() // 2
            // Ensure we have 10 bytes for the heading information
            if (input.readableBytes() < 10) {
                input.readerIndex(startIndex)
                break
            }

            val component = input.readUnsignedShort() // 4
            val command = input.readUnsignedShort() // 6
            val error = input.readUnsignedShort() // 8
            val qtype = input.readUnsignedShort() // 10
            val id = input.readUnsignedShort() // 12


            val extLength: Int = if ((qtype and 0x10) != 0) {
                if (input.readableBytes() < 2) {
                    input.readerIndex(startIndex)
                    break
                }
                input.readUnsignedShort()
            } else {
                0
            }

            val contentLength = length + (extLength shl 16)
            if (input.readableBytes() < contentLength) {
                input.readerIndex(startIndex)
                break
            }
            val content = Unpooled.buffer(contentLength, contentLength)
            input.readBytes(content, contentLength)// Read the bytes into a new buffer and use that as content
            val packet = LazyBufferPacket(component, command, error, qtype, id, content)

            if (PacketLogger.isEnabled) {
                PacketLogger.logDebug("DECODED PACKET", ctx.channel(), packet)
            }
            ctx.fireChannelRead(packet)
            read = true
        }
        return read
    }


    private fun mergeCumulate(alloc: ByteBufAllocator, cumulation: ByteBuf, input: ByteBuf): ByteBuf {
        if (!cumulation.isReadable && input.isContiguous) {
            cumulation.release()
            return input
        }
        try {
            val required = input.readableBytes()
            if (required > cumulation.maxWritableBytes()
                || required > cumulation.maxFastWritableBytes() && cumulation.refCnt() > 1
                || cumulation.isReadOnly
            ) {
                val oldBytes = cumulation.readableBytes()
                val newBytes = input.readableBytes()
                val totalBytes = oldBytes + newBytes
                val newCumulation = alloc.buffer(alloc.calculateNewCapacity(totalBytes, Int.MAX_VALUE))
                var toRelease = newCumulation
                try {
                    newCumulation.setBytes(0, cumulation, cumulation.readerIndex(), oldBytes)
                        .setBytes(oldBytes, input, input.readerIndex(), newBytes)
                        .writerIndex(totalBytes)
                    input.readerIndex(input.writerIndex())
                    toRelease = cumulation
                    return newCumulation
                } finally {
                    toRelease.release()
                }
            }
            cumulation.writeBytes(input, input.readerIndex(), required)
            input.readerIndex(input.writerIndex())
            return cumulation
        } finally {
            input.release()
        }
    }


    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        numReads = 0
        discardSomeReadBytes()
        if (selfFiredChannelRead && !firedChannelRead && !ctx.channel().config().isAutoRead) {
            ctx.read()
        }
        firedChannelRead = false
        ctx.fireChannelReadComplete()
    }

    private fun discardSomeReadBytes() {
        val cumulation = cumulation
        if (cumulation != null && !first && cumulation.refCnt() == 1) {
            cumulation.discardSomeReadBytes()
        }
    }
}