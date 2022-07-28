import com.jacobtread.blaze.tdf.*
import com.jacobtread.blaze.data.VarPair
import com.jacobtread.blaze.data.VarTriple
import com.jacobtread.blaze.tdf.types.*
import io.netty.buffer.Unpooled
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextULong
import kotlin.test.Test
import kotlin.test.assertEquals

typealias Generator<T> = () -> T

/**
 * TdfTest JUnit tests for TDF functionality to ensure
 * that TDFs can be both encoded and decoded to produce
 * the same value
 *
 * @constructor Create empty TdfTest
 */
internal class TdfTest {

    companion object {
        const val ITERATIONS = 100

        private const val ABC = "abcdefghijklmnopqrstuvwxyz"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = "\\/[]{}()_+!`~@#\$%^&*<>,.?;'\":|"
        private val CHARS: CharArray = (ABC + ABC.uppercase() + NUMBERS + SYMBOLS).toCharArray()
        private val BUFFER = Unpooled.buffer();

        fun createRandomString(length: Int): String {
            return buildString { repeat(length) { append(CHARS.random()) } }
        }


        val LabelGen: Generator<String> = {
            val possibleChars = ABC.uppercase().toCharArray()
            val chars = CharArray(4) { possibleChars.random() }
            chars.concatToString()
        }

        private val ULongGen: Generator<ULong> = {
            val maxValue = ULong.MAX_VALUE
            val minValue = ULong.MIN_VALUE
            Random.nextULong(minValue..maxValue)
        }
        private val StringGen: Generator<String> = {
            val minLength = 0
            val maxLength = 1000
            val length = Random.nextInt(minLength..maxLength)
            createRandomString(length)
        }
        private val TrippleGen: Generator<VarTriple> = { VarTriple(ULongGen(), ULongGen(), ULongGen()) }
        private val PairGen: Generator<VarPair> = { VarPair(ULongGen(), ULongGen()) }
        private val FloatGen: Generator<Float> = { Random.nextFloat() }

        private val VarIntTdfGen: Generator<VarIntTdf<*>> = {
            val value = LabelGen()
            ULongTdf(value, ULongGen())
        }
        private val StringTdfGen: Generator<StringTdf> = {
            StringTdf(LabelGen(), StringGen())
        }

        private val BlobTdfGen: Generator<BlobTdf> = {
            val minLength = 0
            val maxLength = 500
            val arrayLength = Random.nextInt(minLength..maxLength)
            val value = Random.nextBytes(arrayLength)
            BlobTdf(LabelGen(), value)
        }

        private val GroupTdfGen: Generator<GroupTdf> = {
            val minValues = 0
            val maxValues = 5
            val values = Random.nextInt(minValues..maxValues)
            val valueList = ArrayList<Tdf<*>>(values)
            repeat(values) { valueList.add(createRandomTdf()) }
            val isTwo = Random.nextBoolean()
            GroupTdf(LabelGen(), isTwo, valueList)
        }

        private val UnnamedGroupTdfGen: Generator<GroupTdf> = {
            val minValues = 0
            val maxValues = 5
            val values = Random.nextInt(minValues..maxValues)
            val valueList = ArrayList<Tdf<*>>(values)
            repeat(values) { valueList.add(createRandomTdf()) }
            val isTwo = Random.nextBoolean()
            GroupTdf("", isTwo, valueList)
        }

        private val ListTdfGen: Generator<ListTdf> = {
            val minValues = 0
            val maxValues = 20
            val values = Random.nextInt(minValues..maxValues)
            val valueType = Random.nextInt(0..100)
            val listContents = ArrayList<Any>(values)
            val listType: UByte = when (valueType) {
                1 -> {
                    repeat(values) { listContents.add(StringGen()) }
                    Tdf.STRING
                }
                in 48 .. 50 -> {
                    repeat(values) { listContents.add(UnnamedGroupTdfGen()) }
                    Tdf.GROUP
                }
                3 -> {
                    repeat(values) { listContents.add(TrippleGen()) }
                    Tdf.TRIPPLE
                }
                else -> {
                    repeat(values) { listContents.add(ULongGen()) }
                    Tdf.VARINT
                }
            }
            ListTdf(LabelGen(), listType, listContents)
        }

        private val MapTdfGen: Generator<MapTdf> = {
            val minValues = 0
            val maxValues = 20
            val values = Random.nextInt(minValues..maxValues)
            val keyTypeRaw = Random.nextInt(0..1)
            val keyType = when (keyTypeRaw) {
                0 -> Tdf.VARINT
                1 -> Tdf.STRING
                else -> throw IllegalArgumentException("Unexpected value")
            }
            val valueTypeRaw = Random.nextInt(0..100)
            val valueType = when (valueTypeRaw) {
                1 -> Tdf.STRING
                100 -> Tdf.GROUP
                3 -> Tdf.FLOAT
                else -> Tdf.VARINT
            }
            val map = LinkedHashMap<Any, Any>()

            repeat(values) {
                val key = when (keyTypeRaw) {
                    0 -> ULongGen()
                    1 -> StringGen()
                    else -> throw IllegalArgumentException("Unexpected value")
                }
                val value: Any = when (valueTypeRaw) {
                    1 -> StringGen()
                    100 -> UnnamedGroupTdfGen()
                    3 -> FloatGen()
                    else -> ULongGen()
                }
                map[key] = value
            }
            MapTdf(LabelGen(), keyType, valueType, map)
        }

        private val OptionalTdfGen: Generator<OptionalTdf> = {
            val type = Random.nextInt(0 until 100).toUByte()
            val value = createRandomTdf()
            OptionalTdf(LabelGen(), type, value)
        }

        private val VarIntListTdfTdfGen: Generator<VarIntListTdf> = {
            val minValues = 0
            val maxValues = 20
            val values = Random.nextInt(minValues..maxValues)
            val valuesList = ArrayList<ULong>(values)
            repeat(values) { valuesList.add(ULongGen()) }
            VarIntListTdf(LabelGen(), valuesList)
        }

        private val PairTdfGen: Generator<PairTdf> = { PairTdf(LabelGen(), PairGen()) }
        private val TrippleTdfGen: Generator<TrippleTdf> = { TrippleTdf(LabelGen(), TrippleGen()) }
        private val FloatTdfGen: Generator<FloatTdf> = { FloatTdf(LabelGen(), FloatGen()) }

        private fun createRandomTdf(): Tdf<*> {
            val generator: Generator<Tdf<*>> = when (Random.nextInt(0..100)) {
                1 -> StringTdfGen
                2 -> BlobTdfGen
                100 -> GroupTdfGen
                4 -> ListTdfGen
                5 -> MapTdfGen
                6 -> OptionalTdfGen
                7 -> VarIntListTdfTdfGen
                8 -> PairTdfGen
                9 -> TrippleTdfGen
                10 -> FloatTdfGen
                else -> VarIntTdfGen
            }
            return generator()
        }

        private fun testTdfIterations(generator: Generator<Tdf<*>>) {
            repeat(ITERATIONS) { // Repeat for the total iterations
                BUFFER.discardReadBytes() // Discard the existing buffer contents and reset
                val value = generator() // Generate a tdf value
                value.writeFully(BUFFER) // Write the tdf to the buffer
                val readTdf = Tdf.read(BUFFER) // Read the tdf from the buffer
                assertEquals(value, readTdf) // Assert the read value matches
            }
        }
    }

    /**
     * test varint Tests the VarInt encoding against
     * a variety of different number values
     */
    @Test
    fun `test varint`() = testTdfIterations(VarIntTdfGen)


    /**
     * test string Tests the string encoding against
     * a randomized string between 0 and 1000 characters
     * long
     */
    @Test
    fun `test string`() = testTdfIterations(StringTdfGen)

    /**
     * test blob Tests the blob (ByteArray) encoding on random
     * byte arrays with lengths between 0 and 500
     */
    @Test
    fun `test blob`() = testTdfIterations(BlobTdfGen)

    /**
     * test group Tests the group encoding using random Tdf values
     */
    @Test
    fun `test group`() = testTdfIterations(GroupTdfGen)

    @Test
    fun `test list`() = testTdfIterations(ListTdfGen)

    @Test
    fun `test map`() = testTdfIterations(MapTdfGen)

    @Test
    fun `test optional`() = testTdfIterations(OptionalTdfGen)

    @Test
    fun `test var int list`() = testTdfIterations(VarIntListTdfTdfGen)

    @Test
    fun `test pair`() = testTdfIterations(PairTdfGen)

    @Test
    fun `test tripple`() = testTdfIterations(TrippleTdfGen)

    @Test
    fun `test float`() = testTdfIterations(FloatTdfGen)
}