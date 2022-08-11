package com.jacobtread.blaze.packet

import com.jacobtread.blaze.TdfContainer
import com.jacobtread.blaze.handler.PacketDecoder
import com.jacobtread.blaze.handler.PacketEncoder
import com.jacobtread.blaze.tdf.Tdf
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.handler.ssl.SslContext

/**
 * Represents a network packet which can be sent and received.
 * This abstraction exists to allow packets created on the server
 * and those that are received over network to have different content
 * implementations to improve performance and unnecessary allocations.
 *
 * Implementations:
 * - [LazyBufferPacket] received packets
 * - [ResponsePacket] response packets
 * - [RequestPacket] request packets
 * - [NotifyPacket] notification packets.
 * - [ErrorPacket] packet responses that are errors
 */
interface Packet : TdfContainer {
    val component: Int
    val command: Int
    val error: Int
    val type: Int
    val id: Int

    /**
     * The Tdf contents stored inside this packet.
     */
    val content: List<Tdf<*>>

    /**
     * Calculates the size in bytes that a buffer
     * would need to be to fit the contents of this
     * packet.
     *
     * @return The size in bytes the buffer needs to be
     */
    fun computeContentSize(): Int

    /**
     * Handles writing the contents of this packet
     * to the provided output buffer.
     *
     * @param out The buffer to write to
     */
    fun writeContent(out: ByteBuf)

    /**
     * Implementation for [TdfContainer] to find a tdf from the
     * contents of this packet using its label.
     *
     * @param label The label of the Tdf to find
     * @return The found tdf or null none were found
     */
    override fun getTdfByLabel(label: String): Tdf<*>? = content.find { it.label == label }

    companion object {
        /**
         * The type given to messages that are being sent by the client. These
         * messages are given unique IDs and expect a response from the server.
         *
         * client -> server
         */
        const val REQUEST_TYPE = 0x0000

        /**
         * The type given to messages that are responding to messages of the
         * [REQUEST_TYPE]. These messages have the same component, command,
         * and ID as the corresponding message of [REQUEST_TYPE]
         *
         * server -> client
         */
        const val RESPONSE_TYPE = 0x1000

        /**
         * The type given to messages that are sent by the server which aren't
         * responding to any previous messages (e.g. to notify that a new player
         * has joined).
         *
         * server -> client
         */
        const val NOTIFY_TYPE = 0x2000

        /**
         * The type given to packets which in other cases would be of type
         * [RESPONSE_TYPE] but an error occurred while processing. Packets with
         * this type will have the same unique ID as their corresponding
         * [REQUEST_TYPE] but the error value will be set
         */
        const val ERROR_TYPE = 0x3000 // Packet type representing a packet with an error

        /**
         * Handles releasing packets if they are of [LazyBufferPacket]
         * type. This releases the underlying buffer for the packet if
         * it still has a reference count > 0
         *
         * @param packet The packet to attempt to release
         */
        fun release(packet: Packet) {
            if (packet is LazyBufferPacket) {
                val contentBuffer = packet.contentBuffer
                if (contentBuffer.refCnt() > 0) {
                    contentBuffer.release(contentBuffer.refCnt())
                }
            }
        }

        /**
         * Obtains a weak reference to the provided packet. This
         * is used in cases where you want the basic information
         * about a packet but don't want to persist a reference
         * to that packet.
         *
         * @param packet The packet to get the weak reference for
         * @return The created weak reference
         */
        fun getWeakReference(packet: Packet): WeakPacketRef = WeakPacketRef(packet)

        /**
         * Append the required decoding and encoding
         * handlers to the provided channel.
         *
         * Optionally if an [SslContext] is provided the
         * SSL handler will be added as well
         *
         * @receiver The channel to append the handlers too
         * @param sslContext Optional ssl context for secure
         */
        fun Channel.addPacketHandlers(sslContext: SslContext? = null) {
            val pipeline = pipeline()
                .addFirst(PacketDecoder())
                .addLast(PacketEncoder)
            if (sslContext != null) {
                pipeline.addFirst(sslContext.newHandler(alloc()))
            }
        }
    }
}