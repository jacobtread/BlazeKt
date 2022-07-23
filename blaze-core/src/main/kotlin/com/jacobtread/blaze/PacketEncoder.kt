package com.jacobtread.blaze

import com.jacobtread.blaze.packet.Packet
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.EncoderException
import io.netty.util.AttributeKey
import io.netty.util.ReferenceCountUtil

object PacketEncoder : ChannelOutboundHandlerAdapter() {

    val ENCODER_CONTEXT_KEY: AttributeKey<String> = AttributeKey.newInstance("EncoderContext")

    override fun isSharable(): Boolean = true

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        var buffer: ByteBuf? = null
        try {
            if (msg is Packet) {
                buffer = Packet.allocateBuffer(ctx.alloc(), msg)
                try {
                    if (PacketLogger.isEnabled) {
                        PacketLogger.logDebug("ENCODED PACKET", ctx.channel(), msg)
                    }
                    msg.writeTo(buffer)
                    ctx.flush()
                } finally {
                    ReferenceCountUtil.release(msg)
                }
                if (buffer.isReadable) {
                    ctx.write(buffer, promise)
                } else {
                    buffer.release()
                    ctx.write(Unpooled.EMPTY_BUFFER, promise)
                }
                buffer = null
            } else {
                ctx.write(msg, promise)
            }
        } catch (e: EncoderException) {
            throw e
        } catch (e: Throwable) {
            throw EncoderException(e)
        } finally {
            buffer?.release()
        }
    }

}