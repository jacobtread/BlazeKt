import com.jacobtread.blaze.*
import com.jacobtread.blaze.data.VarPair
import com.jacobtread.blaze.data.VarTriple
import com.jacobtread.blaze.tdf.types.GroupTdf
import kotlin.test.*

internal class TdfContainerTest {

    @Test
    fun `test text tdf content`() {
        val notifyPacket = notify(0x0, 0x0) {
            text("HI", "abc")
        }
        try {
            assertEquals(notifyPacket.text("HI"), "abc")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test varint tdf content`() {
        val notifyPacket = notify(0x0, 0x0) {
            number("HI", 1)
        }
        try {
            assertEquals(notifyPacket.ulong("HI"), 1uL)
            assertEquals(notifyPacket.long("HI"), 1L)
            assertEquals(notifyPacket.int("HI"), 1)
            assertEquals(notifyPacket.uint("HI"), 1u)
            assertEquals(notifyPacket.short("HI"), 1)
            assertEquals(notifyPacket.ushort("HI"), 1u)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test float tdf content`() {
        val notifyPacket = notify(0x0, 0x0) {
            float("HI", 2f)
        }
        try {
            assertEquals(notifyPacket.float("HI"), 2f)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test blob tdf content`() {
        val byteArray = byteArrayOf(0, 5, 12, 32, 3, 51, 1)
        val notifyPacket = notify(0x0, 0x0) {
            blob("HI", byteArray)
        }
        try {
            assertContentEquals(notifyPacket.blob("HI"), byteArray)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test optional tdf content`() {
        val notifyPacket = notify(0x0, 0x0) {
            optional("HI", group { })
        }
        try {
            notifyPacket.optional("HI")
            assertNotNull(notifyPacket.optionalValue("HI"))
            assertIs<GroupTdf>(notifyPacket.optionalValue("HI"))
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test group tdf content`() {
        val notifyPacket = notify(0x0, 0x0) {
            +group("HI") { }
        }
        try {
            notifyPacket.group("HI")
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test triple tdf content`() {
        val triple = VarTriple(0u, 3u, 5u)
        val notifyPacket = notify(0x0, 0x0) {
            tripple("HI", triple)
        }
        try {
            assertEquals(notifyPacket.triple("HI"), triple)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test pair tdf content`() {
        val triple = VarPair(0u, 3u)
        val notifyPacket = notify(0x0, 0x0) {
            pair("HI", triple)
        }
        try {
            assertEquals(notifyPacket.pair("HI"), triple)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test varint list tdf content`() {
        val list = listOf<ULong>(0u,5u,12u)
        val notifyPacket = notify(0x0, 0x0) {
            varList("HI", list)
        }
        try {
            assertEquals(notifyPacket.varIntList("HI"), list)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test list tdf content`() {
        val list = listOf("", "123", "21313")
        val notifyPacket = notify(0x0, 0x0) {
            list("HI", list)
        }
        try {
            assertContentEquals(notifyPacket.list("HI"), list)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }

    @Test
    fun `test map tdf content`() {
        val map = mapOf("123" to "1313", "teaw" to "123123")
        val notifyPacket = notify(0x0, 0x0) {
            map("HI", map)
        }
        try {
            assertEquals(notifyPacket.map("HI"), map)
        } catch (e: TdfContainerException) {
            throw AssertionError("Failed to get content correctly ", e)
        }
    }
}