package com.jacobtread.blaze

import com.jacobtread.blaze.packet.LazyBufferPacket
import io.netty.buffer.Unpooled
import java.nio.file.Files
import java.nio.file.Paths

/**
 * main Simple program takes all the content from the replays' directory
 * (Packet logs etc.) and reads all the packet and places the decoded
 * variants into the decoded directory
 */
fun main() {
    val dir = Paths.get("data/replay")
    val decodedDir = dir.resolve("decoded")

    if (Files.notExists(decodedDir)) Files.createDirectories(decodedDir)
    Files.newDirectoryStream(dir).forEach {
        if (it.fileName.toString().endsWith(".bin")) {
            val outFile = decodedDir.resolve("${it.fileName}.txt")
            val outFileRaw = decodedDir.resolve("${it.fileName}.raw.txt")
            if (Files.notExists(outFile)) Files.createFile(outFile)
            val outBuilder = StringBuilder()
            val contents = Files.readAllBytes(it)
            val buffer = Unpooled.wrappedBuffer(contents)
            val bufferRaw = StringBuilder()
            while (buffer.readableBytes() > 0) {
                try {
                    val length = buffer.readUnsignedShort();
                    val component = buffer.readUnsignedShort()
                    val command = buffer.readUnsignedShort()
                    val error = buffer.readUnsignedShort()
                    val qtype = buffer.readUnsignedShort()
                    val id = buffer.readUnsignedShort()
                    val extLength = if ((qtype and 0x10) != 0) buffer.readUnsignedShort() else 0
                    val contentLength = length + (extLength shl 16)
                    val content = buffer.readBytes(contentLength)
                    content.markReaderIndex()
                    val packet = LazyBufferPacket(component, command, error, qtype, id, content)
                    PacketLogger.createPacketSource(outBuilder, packet)
                    outBuilder.append('\n')
                    content.resetReaderIndex()
                    var count = 0
                    while (content.readableBytes() > 0) {
                        val byte = content.readUnsignedByte()
                        bufferRaw
                            .append(byte.toInt() and 255)
                            .append(", ")
                        count++
                        if (count == 12) {
                            bufferRaw.append('\n')
                            count = 0
                        }
                    }
                    bufferRaw.appendLine()
                    bufferRaw.appendLine()
                    content.release()

                } catch (e: Throwable) {
                    e.printStackTrace()
                    break
                }
            }
            buffer.readerIndex(0)
            Files.writeString(outFile, outBuilder.toString())
            Files.writeString(outFileRaw, bufferRaw.toString())
        }
    }
}