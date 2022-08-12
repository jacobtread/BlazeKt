package com.jacobtread.blaze.handler

import com.jacobtread.blaze.logging.PacketLogger
import com.jacobtread.blaze.packet.LazyBufferPacket
import com.jacobtread.blaze.packet.Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise

/**
 * Handler for encoding packets into [ByteBuf]'s this handler
 * is an object and can be shared amongst many channels because
 * it has no state.
 *
 * @constructor Create empty Packet encoder
 */
object PacketEncoder : ChannelOutboundHandlerAdapter() {

    /**
     * Whether this handler can be shared between
     * multiple channels or not. In this case it
     * can because there is no stored state.
     *
     * @return In this use case always true
     */
    override fun isSharable(): Boolean = true

    /**
     * Handles writing messages. Specifically this handles converting
     * [Packet]'s into [ByteBuf]'s and then writing them to the pipeline
     * so that they are sent to the client
     *
     * @param ctx The channel handler context
     * @param msg The message itself (only handled if it's a [Packet] other types are passed along)
     * @param promise The channel promise
     */
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is Packet) { // Handle Packets
            val contentSize = msg.computeContentSize()
            val isExtended = contentSize > 0xFFFF
            val bufferSize = 12 + contentSize + (if (isExtended) 2 else 0)
            // Allocate a buffer for the packet size
            val buffer: ByteBuf = ctx.alloc()
                .ioBuffer(bufferSize, bufferSize)
            buffer.writeShort(contentSize) // Length of the packet content
            buffer.writeShort(msg.component) // Packet component value
            buffer.writeShort(msg.command) // Packet command value
            buffer.writeShort(msg.error) // Packet error value
            buffer.writeByte(msg.type shr 8) // Packet type value
            buffer.writeByte(if (isExtended) 0x10 else 0x00) // Whether the packet is extended
            buffer.writeShort(msg.id) // Packet id
            if (isExtended) {
                buffer.writeByte(((contentSize.toLong() and 0xFF000000) shr 24).toInt())
                buffer.writeByte((contentSize and 0x00FF0000) shr 16)
            }

            // Write the packet contents
            msg.writeContent(buffer)
            ctx.flush()
            // Log the encoded packet
            if (PacketLogger.isEnabled) {
                PacketLogger.log("ENCODED PACKET", ctx.channel(), msg)
            }
            // Write the packet buffer
            ctx.write(buffer, promise)
            ctx.flush()
        } else { // Write everything else back again
            ctx.write(msg, promise)
        }
    }
}