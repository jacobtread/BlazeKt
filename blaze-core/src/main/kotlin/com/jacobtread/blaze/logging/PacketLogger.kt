package com.jacobtread.blaze.logging

import com.jacobtread.blaze.logging.PacketLogger.init
import com.jacobtread.blaze.data.VarTriple
import com.jacobtread.blaze.packet.LazyBufferPacket
import com.jacobtread.blaze.packet.Packet
import com.jacobtread.blaze.tdf.Tdf
import com.jacobtread.blaze.tdf.types.*
import io.netty.channel.Channel
import io.netty.util.AttributeKey

/**
 * Logger implementation for logging debug information
 * about incoming and outgoing packets. [init]
 *
 * @constructor Create empty Packet logger
 */
object PacketLogger {

    /**
     * Channel attribute key for storing information about the channel
     * used for extra logging information while decoding / encoding packets
     */
    private val PACKET_CONTEXT_KEY: AttributeKey<String> = AttributeKey.newInstance("PacketContext")

    private var debugComponentNames: Map<Int, String>? = null
    private var debugCommandNames: Map<Int, String>? = null
    private var debugNotifyNames: Map<Int, String>? = null

    var handler: BlazeLoggingHandler? = null

    var isEnabled: Boolean = false

    /**
     * Initialize the logger with the provided naming
     * configuration and logging output pipe. This enables
     * logging through this logger.
     *
     * @param handler The handler for retrieving the debug command names and doing logging
     */
    fun init(handler: BlazeLoggingHandler?, ) {
        if (handler != null) {
            debugComponentNames = handler.getComponentNames()
            debugCommandNames = handler.getCommandNames()
            debugNotifyNames = handler.getNotifyNames()
        }
        PacketLogger.handler = handler
        isEnabled = true
    }

    /**
     * Sets the value of the [PACKET_CONTEXT_KEY] for the
     * provided channel to [value]
     *
     * @param channel The channel to set the context value for
     * @param value The value for the context
     */
    fun setContext(channel: Channel, value: String) {
        channel.attr(PACKET_CONTEXT_KEY)
            .set(value)
    }

    /**
     * Logs a packet to the debug output using the provided [handler]
     * logging pipe.
     *
     * @param title The title of the message to log
     * @param channel The channel the message was read from / written to used to retrieve the [PacketEncoder.ENCODER_CONTEXT_KEY]
     * @param packet The packet to log
     */
    fun log(title: String, channel: Channel, packet: Packet) {
        try {
            val lineWidth = 30
            val out = StringBuilder()
            out.append(title)
                .append(' ')
            repeat(lineWidth - (title.length + 1)) { out.append('=') }
            out.appendLine()

            val contextAttr: String? = channel.attr(PACKET_CONTEXT_KEY)
                .get()

            if (contextAttr != null) {
                out.appendLine(contextAttr)
            }
            createPacketSource(out, packet)
            out.appendLine()
            repeat(lineWidth) {
                out.append('=')
            }
            out.appendLine()
            handler?.debug(out.toString())
        } catch (e: Throwable) {
            dumpPacketException(packet, e)
        }
    }

    /**
     * Dumps the packet information in raw form. Used for when creating
     * the source representation fails.
     *
     * @param packet The packet to dump
     * @param cause The cause of the failure
     */
    fun dumpPacketException(packet: Packet, cause: Throwable) {
        try {
            val out = StringBuilder("Failed to decode packet contents for debugging: ")
                .appendLine()
                .appendLine("Packet Information ==================================")
                .append("Component: 0x")
                .append(packet.component.toString(16))
                .append(' ')
                .append(debugComponentNames?.get(packet.component) ?: "UNKNOWN")
                .appendLine()
                .append("Command: 0x")
                .append(packet.command.toString(16))
                .append(' ')
            val isNotify = packet.type == Packet.NOTIFY_TYPE
            val commandNameIndex = (packet.component shl 16) + packet.command
            val commandName: String? = if (isNotify) {
                debugNotifyNames?.get(commandNameIndex)
                    ?: debugCommandNames?.get(commandNameIndex)
            } else {
                debugCommandNames?.get(commandNameIndex)
            }
            out
                .append(commandName ?: "UNKNOWN")
                .appendLine()
                .append("Error: 0x")
                .append(packet.error.toString(16))
                .appendLine()
                .append("Type: ")
                .append(
                    when (packet.type) {
                        Packet.INCOMING_TYPE -> "INCOMING"
                        Packet.ERROR_TYPE -> "ERROR"
                        Packet.NOTIFY_TYPE -> "NOTIFY"
                        Packet.RESPONSE_TYPE -> "RESPONSE"
                        else -> "UNKNOWN"
                    }
                )
                .append(" (0x")
                .append(packet.type.toString(16))
                .append(')')
                .appendLine()

                .append("ID: 0x")
                .append(packet.id.toString(16))
                .appendLine()
                .append("Cause: ")
                .append(cause.message)
                .appendLine()
                .append(cause.stackTraceToString())
                .appendLine()



            if (packet is LazyBufferPacket) {
                out.append("Content Dump:")
                    .appendLine()
                val content = packet.contentBuffer
                try {
                    content.readerIndex(0)
                    var count = 0
                    while (content.readableBytes() > 0) {
                        val byte = content.readUnsignedByte()
                        out
                            .append(byte.toUByte().toString())
                            .append(", ")
                        count++
                        if (count == 12) {
                            out.append('\n')
                            count = 0
                        }
                    }
                } catch (e: Throwable) {
                    out.append("Failed to encode packet raw contents:")
                        .append(e.stackTraceToString())
                }
            }

            out.appendLine()
                .appendLine("=====================================================")
            handler?.warn(out.toString())
        } catch (e: Throwable) {
            handler?.warn("Exception when handling packet dump exception", e)
        }
    }

    /**
     * Creates a human-readable representation of a packet. This representation
     * closely represents the builder structure that is present in this library.
     *
     * @param out The string builder to append the created packet source to
     * @param packet The packet to create the source for
     */
    fun createPacketSource(out: StringBuilder, packet: Packet) {
        out.append("packet(") // Initial opening packet tag

        val componentName = debugComponentNames?.get(packet.component)

        if (componentName != null) {
            out.append("Components.")
                .append(componentName)
        } else {
            out.append("0x")
                .append(packet.component.toString(16))
        }

        out.append(", ")

        val isNotify = packet.type == Packet.NOTIFY_TYPE
        val commandNameIndex = (packet.component shl 16) + packet.command

        val commandName: String? = if (isNotify) {
            debugNotifyNames?.get(commandNameIndex)
                ?: debugCommandNames?.get(commandNameIndex)
        } else {
            debugCommandNames?.get(commandNameIndex)
        }

        if (commandName != null) {
            out.append("Commands.")
                .append(commandName)
        } else {
            out.append("0x")
                .append(packet.command.toString(16))
        }

        out.append(", ")

        when (packet.type) {
            Packet.INCOMING_TYPE -> out.append("INCOMING_TYPE")
            Packet.RESPONSE_TYPE -> out.append("RESPONSE_TYPE")
            Packet.NOTIFY_TYPE -> out.append("NOTIFY_TYPE")
            Packet.ERROR_TYPE -> out.append("ERROR_TYPE")
            else -> out.append("0x")
                .append(packet.type.toString(16))
        }

        if (packet.type == Packet.ERROR_TYPE) {
            out.append(", 0x")
                .append(packet.error.toString(16))
        }

        if (packet.type == Packet.INCOMING_TYPE || packet.type == Packet.RESPONSE_TYPE) {
            out.append(", 0x")
                .append(packet.id.toString(16))
        }

        out.appendLine(") {")

        packet.content.forEach {
            createTdfSource(out, 1, it, false)
            out.appendLine()
        }

        out.append('}')
    }

    /**
     * Appends the indentation level to the provided the string
     * builder each level of [indent] is represented with two
     * spaces
     *
     * @param out The string builder to append to
     * @param indent The indent level
     */
    private fun appendIndent(out: StringBuilder, indent: Int) {
        repeat(indent) { // Append the indentation to output
            out.append("  ")
        }
    }

    /**
     * Creates a human-readable string representation of the provided tdf
     * value in the form that closely resembles the tdf builder source
     *
     * @param out The string builder to append to
     * @param indent The current source indentation level
     * @param value The tdf value itself
     * @param inline Whether this element is inline (e.g. inside a list)
     */
    fun createTdfSource(out: StringBuilder, indent: Int, value: Tdf<*>, inline: Boolean) {
        appendIndent(out, indent)
        when (value) {
            is BlobTdf -> {
                out.append("blob(\"")
                    .append(value.label)
                    .append('"')
                val byteArray = value.value
                val size = byteArray.size
                if (size > 0) {
                    out.append(", byteArrayOf(")
                    byteArray.joinTo(out, ", ") { (it.toInt() and 0xFF).toString() }
                    out.append(')')
                }
                out.append(')')
            }

            is FloatTdf -> {
                out.append("float(\"")
                    .append(value.label)
                    .append("\", ")
                    .append(value.value)
                    .append(')')
            }

            is GroupTdf -> {
                if (!inline) out.append('+')

                out.append("group")
                if (value.label.isNotEmpty()) {
                    out.append("(\"")
                        .append(value.label)
                        .append('"')
                    if (value.start2) out.append(", true")
                    out.append(')')
                } else {
                    if (value.start2) out.append("(start2=true)")
                }
                out.appendLine(" {")
                val values = value.value
                values.forEach {
                    createTdfSource(out, indent + 1, it, false)
                    out.appendLine()
                }
                appendIndent(out, indent)
                out.append('}')
            }

            is ListTdf -> {
                out.append("list(\"")
                    .append(value.label)
                    .append("\", listOf(")
                val values = value.value
                when (value.type) {
                    Tdf.VARINT -> {
                        when (values[0]) {
                            is ULong -> values.joinTo(out, ", ") { (it as ULong).toString(16) }
                            is Long -> values.joinTo(out, ", ") { (it as Long).toString(16) }
                            is Int -> values.joinTo(out, ", ") { (it as Int).toString(16) }
                            is UInt -> values.joinTo(out, ", ") { (it as UInt).toString(16) }
                            else -> values.joinTo(out, ", ")
                        }
                    }

                    Tdf.STRING -> {
                        values.joinTo(out, ", ")
                    }

                    Tdf.TRIPPLE -> {
                        values.joinTo(out, ", ") {
                            val tripple = it as VarTriple
                            val a = tripple.a.toString(16)
                            val b = tripple.b.toString(16)
                            val c = tripple.c.toString(16)
                            "VarTripple(0x$a, 0x$b, 0x$c)"
                        }
                    }

                    Tdf.GROUP -> {
                        out.appendLine()
                        val size = values.size
                        for (i in 0 until size) {
                            val valueAt = values[i] as GroupTdf
                            createTdfSource(out, indent + 1, valueAt, true)
                            if (i != size - 1) {
                                out.append(',')
                            }
                            out.appendLine()
                        }
                        appendIndent(out, indent)
                    }

                    else -> values.joinTo(out, ", ") { it.javaClass.simpleName }
                }
                out.append("))")


            }

            is MapTdf -> {
                val map = value.value
                out.append("map(\"")
                    .append(value.label)
                    .appendLine("\", mapOf(")
                map.forEach { (mapKey, mapValue) ->
                    appendIndent(out, indent + 1)
                    when (mapKey) {
                        is String -> out.append('"')
                            .append(mapKey)
                            .append('"')

                        is ULong -> out.append("0x").append(mapKey.toString(16))
                        is UInt -> out.append("0x").append(mapKey.toString(16))
                        is Long -> out.append("0x").append(mapKey.toString(16))
                        is Int -> out.append("0x").append(mapKey.toString(16))
                    }
                    out.append(" to ")
                    when (mapValue) {
                        is String -> out.append('"')
                            .append(mapValue)
                            .append('"')

                        is ULong -> out.append("0x").append(mapValue.toString(16))
                        is UInt -> out.append("0x").append(mapValue.toString(16))
                        is Long -> out.append("0x").append(mapValue.toString(16))
                        is Int -> out.append("0x").append(mapValue.toString(16))
                        is Float -> out.append(mapValue.toString())
                        is GroupTdf -> createTdfSource(out, indent + 1, mapValue, true)
                    }
                    out.appendLine(',')
                }
                appendIndent(out, indent)
                out.append("))")
            }

            is OptionalTdf -> {
                val content = value.value
                out.append("optional(\"")
                    .append(value.label)
                    .append("\", ")
                if (content != null) {
                    out.appendLine()
                    appendIndent(out, indent)
                    out.append("0x")
                        .append(value.type.toString(16))
                    out.appendLine(",")
                    createTdfSource(out, indent + 1, content, true)
                    out.appendLine()
                    appendIndent(out, indent)
                    out.append(')')
                } else {
                    out.append("0x")
                        .append(value.type.toString(16))
                    out.append(", null)")
                }
            }

            is PairTdf -> {
                val pair = value.value
                out.append("pair(\"")
                    .append(value.label)
                    .append("\", 0x")
                    .append(pair.a.toString(16))
                    .append(", 0x")
                    .append(pair.b.toString(16))
                    .append(')')
            }

            is StringTdf -> {
                out.append("text(\"")
                    .append(value.label)
                    .append('"')
                if (value.value.isNotEmpty()) {
                    out.append(", \"")
                        .append(
                            value.value
                                .replace("\n", "\\n")
                        )
                        .append('"')
                }
                out.append(')')
            }

            is TrippleTdf -> {
                val tripple = value.value
                out.append("tripple(\"")
                    .append(value.label)
                    .append("\", 0x")
                    .append(tripple.a.toString(16))
                    .append(", 0x")
                    .append(tripple.b.toString(16))
                    .append(", 0x")
                    .append(tripple.c.toString(16))
                    .append(')')
            }

            is VarIntListTdf -> {
                out.append("varList(\"")
                    .append(value.label)
                    .append("\"")
                val values = value.value
                if (values.isNotEmpty()) {
                    out.append(", listOf(")
                    values.joinTo(out, ", ") { "0x${it.toString(16)}" }
                }
                out.append("))")
            }

            is VarIntTdf<*> -> {
                out.append("number(\"")
                    .append(value.label)
                    .append("\", 0x")
                    .append(value.toULong().toString(16))
                    .append(')')
            }
        }
    }

    /**
     * Logs an error without an exception to
     * the logging pipe
     *
     * @param text The error text
     */
    fun error(text: String) {
        handler?.error(text)
    }

    /**
     * Logs and error with an exception to
     * the logging pipe
     *
     * @param text The error text
     * @param cause The thrown exception
     */
    fun error(text: String, cause: Throwable) {
        handler?.error(text, cause)
    }
}