package com.jacobtread.blaze.handler

import com.jacobtread.blaze.PacketPushable
import com.jacobtread.blaze.error
import com.jacobtread.blaze.logging.PacketLogger
import com.jacobtread.blaze.packet.Packet
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.DecoderException
import java.io.IOException
import javax.net.ssl.SSLException

/**
 * Abstraction from [ChannelInboundHandlerAdapter] for handling
 * packet specific behaviour as well as easily "pushing" packets
 * through the [PacketPushable] interface.
 *
 * Includes handling for lost connections through [handleConnectionLost]
 * and exceptions through [handleException]
 *
 * Packet routing should be done in [handlePacket] this will ensure
 * any exceptions thrown will be passed to [handleException].
 *
 * @constructor Create empty Packet handler
 */
abstract class PacketNettyHandler : ChannelInboundHandlerAdapter(), PacketPushable {

    /**
     * The channel this handler is attached to.
     * Used for pushing packets
     */
    abstract val channel: Channel

    /**
     * Handles function called when an exception is caught either while
     * handling a packet
     *
     * @param ctx The channel handler context
     * @param cause The exception thrown
     * @param packet The packet that was being processed
     */
    open fun handleException(ctx: ChannelHandlerContext, cause: Throwable, packet: Packet) {
        // Empty response for exception handle
        push(packet.error(0x0))
    }


    /**
     * Handler function called when the connection the client is
     * disconnected improperly (i.e. The client looses connection)
     *
     * @param ctx The channel handler context
     */
    open fun handleConnectionLost(ctx: ChannelHandlerContext) {}

    /**
     * Handles incoming packets this call should be passed straight onto
     * the routing function for this handler unless routing is being done
     * manually instead. Exceptions thrown during this function will be
     * caught and call [handleException]
     *
     * @param ctx The channel handler context
     * @param packet The received packet
     */
    abstract fun handlePacket(ctx: ChannelHandlerContext, packet: Packet)

    /**
     * Handles reading messages from the channel pipeline. Only handles
     * messages that are of [Packet] type any other messages are just
     * sent down the pipeline.
     *
     * This function takes care of routing the packets through [handlePacket]
     * as well as flushing the channel and releasing packets that have already
     * been processed
     *
     * @param ctx The channel handler context
     * @param msg The channel message
     */
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is Packet) {
            try {
                handlePacket(ctx, msg) // Route the packet to its destination
                ctx.flush() // Flush written packets
                Packet.release(msg) // Release the incoming packet
            } catch (e: Throwable) {
                handleException(ctx, e, msg)
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    /**
     * Handles pushing packets to be written to the socket
     * connection. Checks if the push request was in the
     * event loop and then writes and flushes the packet
     * otherwise it tells the event loop to execute the
     * same thing.
     *
     * @param packet The packet to write to the socket
     */
    override fun push(packet: Packet) {
        val eventLoop = channel.eventLoop()
        if (eventLoop.inEventLoop()) { // If the push was made inside the event loop
            // Write the packet and flush
            channel.write(packet)
            channel.flush()
        } else { // If the push was made outside the event loop
            eventLoop.execute { // Execute write and flush on event loop
                channel.write(packet)
                channel.flush()
            }
        }
    }

    /**
     * Handles pushing multiple packets to be written to the socket
     * connection. Checks to see if the push request was in the event
     * loop and then writes all then packets before flushing. If it's
     * called outside the event loop it tells the event loop to execute
     * the same instruction
     *
     * @param packets The packets to write to the socket
     */
    override fun pushAll(vararg packets: Packet) {
        val eventLoop = channel.eventLoop()
        if (eventLoop.inEventLoop()) { // If the push was made inside the event loop
            // Write the packets and flush
            packets.forEach { channel.write(it) }
            channel.flush()
        } else { // If the push was made outside the event loop
            eventLoop.execute { // Execute write and flush on event loop
                packets.forEach { channel.write(it) }
                channel.flush()
            }
        }
    }

    /**
     * Handles exceptions caught by the time they reach this point.
     * If the exception is due to a connection lost then the handler
     * [handleConnectionLost] function is called otherwise the exception
     * is passed onto [handleException]
     *
     * If [handleException] is reached then the channel connection
     * is closed.
     *
     * @param ctx The channel handler context
     * @param cause The exception that was caught
     */
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is IOException) {
            val message = cause.message
            if (message != null && message.startsWith("Connection reset")) {
                handleConnectionLost(ctx)
                return
            }
        } else if (cause is DecoderException) {
            val underlying = cause.cause
            if (underlying is SSLException) {
                val ipAddress = channel.remoteAddress()
                PacketLogger.debug("Connection at $ipAddress tried to connect without valid SSL (Did someone try to connect with a browser?)")
                return
            }
        }
        ctx.close()
    }
}