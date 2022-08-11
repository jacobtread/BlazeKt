package com.jacobtread.blaze.handler

import com.jacobtread.blaze.logging.PacketLogger
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
        if (msg is Packet) {
            // The buffer which needs to be released
            var buffer: ByteBuf? = null
            try {
                val contentSize = msg.computeContentSize()
                val isExtended = contentSize > 0xFFFF
                val bufferSize = 12 + contentSize + (if (isExtended) 2 else 0)
                buffer = ctx.alloc().ioBuffer(bufferSize, bufferSize)

                with(buffer) {
                    writeShort(contentSize) // Length of the packet content
                    writeShort(msg.component) // Packet component value
                    writeShort(msg.command) // Packet command value
                    writeShort(msg.error) // Packet error value
                    writeByte(msg.type shr 8) // Packet type value
                    writeByte(if (isExtended) 0x10 else 0x00) // Whether the packet is extended
                    writeShort(msg.id) // Packet id
                    if (isExtended) {
                        writeByte(((contentSize.toLong() and 0xFF000000) shr 24).toInt())
                        writeByte((contentSize and 0x00FF0000) shr 16)
                    }
                    msg.writeContent(this)
                }

                if (PacketLogger.isEnabled) {
                    PacketLogger.log("ENCODED PACKET", ctx.channel(), msg)
                }

                if (buffer.isReadable) {
                    ctx.write(buffer, promise)
                    ctx.flush()
                }
                buffer = null
            } finally {
                buffer?.release()
            }
        } else {
            super.write(ctx, msg, promise)
        }
    }
}