package halotukozak.validation

import halotukozak.validation.ValidationError.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChecksTest {

    private val stringV = Validator<String>(shortCircuit = false) {
        notBlank()
    }

    @Test
    fun `notBlank rejects whitespace`() {
        val r = stringV.validate("   ")
        assertIs<ValidationResult.Invalid>(r)
        assertEquals(Message.NotBlank, r.errors.single().message)
    }

    @Test
    fun `lengthIn enforces range`() {
        val v = Validator<String>(shortCircuit = false) { lengthIn(2..4) }
        assertEquals(ValidationResult.Valid, v.validate("ab"))
        assertEquals(ValidationResult.Valid, v.validate("abcd"))
        assertIs<ValidationResult.Invalid>(v.validate("a"))
        assertIs<ValidationResult.Invalid>(v.validate("abcde"))
    }

    @Test
    fun `maxLength rejects too long`() {
        val v = Validator<String>(shortCircuit = false) { maxLength(3) }
        assertEquals(ValidationResult.Valid, v.validate("abc"))
        assertIs<ValidationResult.Invalid>(v.validate("abcd"))
    }

    @Test
    fun `matches enforces regex`() {
        val v = Validator<String>(shortCircuit = false) {
            matches(Regex("[a-z]+"), Message.Matches("[a-z]+"))
        }
        assertEquals(ValidationResult.Valid, v.validate("abc"))
        assertIs<ValidationResult.Invalid>(v.validate("ABC"))
    }

    @Test
    fun `noLeadingOrTrailingSpace rejects edges`() {
        val v = Validator<String>(shortCircuit = false) { noLeadingOrTrailingSpace() }
        assertEquals(ValidationResult.Valid, v.validate("abc"))
        assertIs<ValidationResult.Invalid>(v.validate(" abc"))
        assertIs<ValidationResult.Invalid>(v.validate("abc "))
    }

    @Test
    fun `noConsecutiveSpaces rejects double space`() {
        val v = Validator<String>(shortCircuit = false) { noConsecutiveSpaces() }
        assertEquals(ValidationResult.Valid, v.validate("a b c"))
        assertIs<ValidationResult.Invalid>(v.validate("a  b"))
    }

    @Test
    fun `inRange works for Int`() {
        val v = Validator<Int>(shortCircuit = false) { inRange(0..10) }
        assertEquals(ValidationResult.Valid, v.validate(5))
        assertIs<ValidationResult.Invalid>(v.validate(-1))
        assertIs<ValidationResult.Invalid>(v.validate(11))
    }

    @Test
    fun `oneOf accepts only members`() {
        val v = Validator<String>(shortCircuit = false) { oneOf(listOf("a", "b")) }
        assertEquals(ValidationResult.Valid, v.validate("a"))
        assertIs<ValidationResult.Invalid>(v.validate("c"))
    }

    @Test
    fun `notEmpty rejects empty list`() {
        val v = Validator<List<Int>>(shortCircuit = false) { notEmpty() }
        assertEquals(ValidationResult.Valid, v.validate(listOf(1)))
        assertIs<ValidationResult.Invalid>(v.validate(emptyList<Int>()))
    }

    @Test
    fun `maxSize enforces upper bound`() {
        val v = Validator<List<Int>>(shortCircuit = false) { maxSize(2) }
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2)))
        assertIs<ValidationResult.Invalid>(v.validate(listOf(1, 2, 3)))
    }

    data class Holder(val items: List<String>)

    @Test
    fun `eachItem reports indexed errors`() {
        val v = Validator<Holder> {
            field(::items) {
                eachItem { notBlank() }
            }
        }
        val r = v.validate(Holder(listOf("ok", "", "")))
        assertIs<ValidationResult.Invalid>(r)
        val elementErrors = r.errors.filterIsInstance<ValidationError.Element>()
        assertEquals(2, elementErrors.size)
        assertEquals(setOf(1, 2), elementErrors.map { it.index }.toSet())
        assertTrue(elementErrors.all { it.path == "items" })
    }

    data class Wrapper(val inner: String?)

    @Test
    fun `optional skips null`() {
        val v = Validator<Wrapper> {
            optional(::inner) { notBlank() }
        }
        assertEquals(ValidationResult.Valid, v.validate(Wrapper(null)))
        assertIs<ValidationResult.Invalid>(v.validate(Wrapper("")))
    }

    data class Outer(val name: String)

    @Test
    fun `validated uses external Validator`() {
        val nameV = Validator<String> { notBlank() }
        val outerV = Validator<Outer> { validated(::name, nameV) }
        assertEquals(ValidationResult.Valid, outerV.validate(Outer("ok")))
        val r = outerV.validate(Outer(""))
        assertIs<ValidationResult.Invalid>(r)
        val err = r.errors.single()
        assertIs<ValidationError.Field>(err)
        assertEquals("name", err.path)
    }
}
