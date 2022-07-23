package com.jacobtread.blaze

import com.jacobtread.blaze.data.VarTripple
import com.jacobtread.blaze.debug.BlazeLoggingOutput
import com.jacobtread.blaze.debug.DebugNaming
import com.jacobtread.blaze.packet.LazyBufferPacket
import com.jacobtread.blaze.packet.Packet
import com.jacobtread.blaze.tdf.*
import io.netty.channel.Channel

object PacketLogger {

    private var debugComponentNames: Map<Int, String>? = null
    private var debugCommandNames: Map<Int, String>? = null
    var output: BlazeLoggingOutput? = null

    var isEnabled: Boolean = false

    fun init(
        componentNames: DebugNaming,
        commandNames: DebugNaming,
        output: BlazeLoggingOutput?,
    ) {
        debugComponentNames = componentNames.getDebugNameMap()
        debugCommandNames = commandNames.getDebugNameMap()
        this.output = output
        isEnabled = true
    }

    fun logDebug(title: String, channel: Channel, packet: Packet) {
        try {
            val lineWidth = 30
            val out = StringBuilder()
            out.append(title)
                .append(' ')
                .appendLine("=".repeat(lineWidth - (title.length + 1)))

            val contextAttr: String? = channel.attr(PacketEncoder.ENCODER_CONTEXT_KEY)
                .get()

            if (contextAttr != null) {
                out.appendLine(contextAttr)
            }


            createPacketSource(out, packet)

            out.appendLine()
                .appendLine("=".repeat(lineWidth))

            debug(out.toString())
        } catch (e: Throwable) {
            dumpPacketException(packet, e)
        }
    }


    private fun dumpPacketException(packet: Packet, cause: Throwable) {
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
                .append(debugCommandNames?.get((packet.component shl 16) + packet.command) ?: "UNKNOWN")
                .appendLine()
                .append("Error: 0x")
                .append(packet.error.toString(16))
                .appendLine()
                .append("Type: ")
                .append(
                    when (packet.type) {
                        Packet.INCOMING_TYPE -> "INCOMING"
                        Packet.ERROR_TYPE -> "ERROR"
                        Packet.UNIQUE_TYPE -> "UNIQUE"
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
            warn(out.toString())
        } catch (e: Throwable) {
            warn("Exception when handling packet dump exception", e)
        }
    }

    fun createPacketSource(out: StringBuilder, packet: Packet): String {
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

        val commandName = debugCommandNames?.get((packet.component shl 16) + packet.command)

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
            Packet.UNIQUE_TYPE -> out.append("UNIQUE_TYPE")
            Packet.ERROR_TYPE -> out.append("ERROR_TYPE")
            else -> out.append("0x")
                .append(packet.type.toString(16))
        }

        out.append(", 0x")
            .append(packet.error.toString(16))
            .appendLine(") {")

        packet.content.forEach {
            createTdfSource(out, 1, it, false)
            out.appendLine()
        }

        out.append('}')
        return out.toString()
    }

    private fun appendIndent(out: StringBuilder, indent: Int) {
        out.append("  ".repeat(indent)) // Append indentation
    }

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
                            val tripple = it as VarTripple
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
            is VarIntTdf -> {
                out.append("number(\"")
                    .append(value.label)
                    .append("\", 0x")
                    .append(value.value.toString(16))
                    .append(')')
            }
        }
    }

    fun debug(text: String) {
        output?.debug(text)
    }

    fun warn(text: String) {
        output?.warn(text)
    }

    fun warn(text: String, cause: Throwable) {
        output?.warn(text, cause)
    }

    fun error(text: String) {
        output?.error(text)
    }

    fun error(text: String, cause: Throwable) {
        output?.error(text, cause)
    }
}