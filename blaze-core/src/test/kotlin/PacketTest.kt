import com.jacobtread.blaze.*
import com.jacobtread.blaze.packet.Packet
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PacketTest {

    @Test
    fun `test create client packet no content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val packet = clientPacket(component, command, id)
        assertEquals(component, packet.component)
        assertEquals(command, packet.command)
        assertEquals(id, packet.id)
        assertEquals(Packet.REQUEST_TYPE, packet.type)
    }


    @Test
    fun `test create response packet no content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val clientPacket = clientPacket(component, command, id)
        val responsePacket = clientPacket.respond()
        assertEquals(component, responsePacket.component)
        assertEquals(command, responsePacket.command)
        assertEquals(id, responsePacket.id)
        assertEquals(Packet.RESPONSE_TYPE, responsePacket.type)
    }

    @Test
    fun `test create error packet no content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val error = 0x404
        val clientPacket = clientPacket(component, command, id)
        val errorPacket = clientPacket.error(error)
        assertEquals(component, errorPacket.component)
        assertEquals(command, errorPacket.command)
        assertEquals(id, errorPacket.id)
        assertEquals(error, errorPacket.error)
        assertEquals(Packet.ERROR_TYPE, errorPacket.type)
    }

    @Test
    fun `test create notify packet no content`() {
        val component = 0x1
        val command = 0x5
        val notifyPacket = notify(component, command)
        assertEquals(component, notifyPacket.component)
        assertEquals(command, notifyPacket.command)
        assertEquals(Packet.NOTIFY_TYPE, notifyPacket.type)
    }

    @Test
    fun `test create client packet content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val packet = clientPacket(component, command, id) {
            number("TEST", 1)
            text("HI", "abc")
        }
        assertEquals(component, packet.component)
        assertEquals(command, packet.command)
        assertEquals(id, packet.id)
        assertEquals(Packet.REQUEST_TYPE, packet.type)
        try {
            assertEquals(packet.int("TEST"), 1)
            assertEquals(packet.text("HI"), "abc")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }


    @Test
    fun `test create response packet content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val clientPacket = clientPacket(component, command, id)
        val responsePacket = clientPacket.respond {
            number("TEST", 1)
            text("HI", "abc")
        }
        assertEquals(component, responsePacket.component)
        assertEquals(command, responsePacket.command)
        assertEquals(id, responsePacket.id)
        assertEquals(Packet.RESPONSE_TYPE, responsePacket.type)
        try {
            assertEquals(responsePacket.int("TEST"), 1)
            assertEquals(responsePacket.text("HI"), "abc")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test create error packet content`() {
        val component = 0x1
        val command = 0x5
        val id = 0x12
        val error = 0x404
        val clientPacket = clientPacket(component, command, id)
        val errorPacket = clientPacket.error(error) {
            number("TEST", 1)
            text("HI", "abc")
        }
        assertEquals(component, errorPacket.component)
        assertEquals(command, errorPacket.command)
        assertEquals(id, errorPacket.id)
        assertEquals(error, errorPacket.error)
        assertEquals(Packet.ERROR_TYPE, errorPacket.type)
        try {
            assertEquals(errorPacket.int("TEST"), 1)
            assertEquals(errorPacket.text("HI"), "abc")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test create notify packet content`() {
        val component = 0x1
        val command = 0x5
        val notifyPacket = notify(component, command) {
            number("TEST", 1)
            text("HI", "abc")
        }
        assertEquals(component, notifyPacket.component)
        assertEquals(command, notifyPacket.command)
        assertEquals(Packet.NOTIFY_TYPE, notifyPacket.type)
        try {
            assertEquals(notifyPacket.int("TEST"), 1)
            assertEquals(notifyPacket.text("HI"), "abc")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }
}