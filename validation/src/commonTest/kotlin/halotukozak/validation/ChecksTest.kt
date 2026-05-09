package halotukozak.validation

import halotukozak.validation.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChecksTest {
    private val stringV =
        Validator<String>(shortCircuit = false) {
            notBlank()
        }

    @Test
    fun `notBlank rejects whitespace`() {
        val r = stringV.validate("   ")
        assertIs<ValidationResult.Invalid>(r)
        assertEquals(Message.NotBlank, r.errors.single().message)
    }

    @Test
    fun `notEmpty String rejects empty but accepts whitespace`() {
        val v = Validator<String>(shortCircuit = false) { notEmpty() }
        assertEquals(ValidationResult.Valid, v.validate("a"))
        assertEquals(ValidationResult.Valid, v.validate(" "))
        assertIs<ValidationResult.Invalid>(v.validate(""))
    }

    @Test
    fun `hasPrefix enforces prefix`() {
        val v = Validator<String>(shortCircuit = false) { hasPrefix("foo") }
        assertEquals(ValidationResult.Valid, v.validate("foobar"))
        assertIs<ValidationResult.Invalid>(v.validate("barfoo"))
    }

    @Test
    fun `hasSuffix enforces suffix`() {
        val v = Validator<String>(shortCircuit = false) { hasSuffix(".kt") }
        assertEquals(ValidationResult.Valid, v.validate("App.kt"))
        assertIs<ValidationResult.Invalid>(v.validate("App.java"))
    }

    @Test
    fun `includes enforces substring`() {
        val v = Validator<String>(shortCircuit = false) { includes("@") }
        assertEquals(ValidationResult.Valid, v.validate("a@b"))
        assertIs<ValidationResult.Invalid>(v.validate("ab"))
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
    fun `minLength rejects too short`() {
        val v = Validator<String>(shortCircuit = false) { minLength(3) }
        assertEquals(ValidationResult.Valid, v.validate("abc"))
        assertEquals(ValidationResult.Valid, v.validate("abcd"))
        assertIs<ValidationResult.Invalid>(v.validate("ab"))
    }

    @Test
    fun `min rejects below`() {
        val v = Validator<Int>(shortCircuit = false) { min(5) }
        assertEquals(ValidationResult.Valid, v.validate(5))
        assertEquals(ValidationResult.Valid, v.validate(10))
        assertIs<ValidationResult.Invalid>(v.validate(4))
    }

    @Test
    fun `max rejects above`() {
        val v = Validator<Int>(shortCircuit = false) { max(5) }
        assertEquals(ValidationResult.Valid, v.validate(5))
        assertEquals(ValidationResult.Valid, v.validate(0))
        assertIs<ValidationResult.Invalid>(v.validate(6))
    }

    @Test
    fun `positive Int`() {
        val v = Validator<Int>(shortCircuit = false) { positive() }
        assertEquals(ValidationResult.Valid, v.validate(1))
        assertIs<ValidationResult.Invalid>(v.validate(0))
        assertIs<ValidationResult.Invalid>(v.validate(-1))
    }

    @Test
    fun `positive Long`() {
        val v = Validator<Long>(shortCircuit = false) { positive() }
        assertEquals(ValidationResult.Valid, v.validate(1L))
        assertIs<ValidationResult.Invalid>(v.validate(0L))
    }

    @Test
    fun `positive Double`() {
        val v = Validator<Double>(shortCircuit = false) { positive() }
        assertEquals(ValidationResult.Valid, v.validate(0.5))
        assertIs<ValidationResult.Invalid>(v.validate(0.0))
        assertIs<ValidationResult.Invalid>(v.validate(-0.1))
    }

    @Test
    fun `negative Int`() {
        val v = Validator<Int>(shortCircuit = false) { negative() }
        assertEquals(ValidationResult.Valid, v.validate(-1))
        assertIs<ValidationResult.Invalid>(v.validate(0))
        assertIs<ValidationResult.Invalid>(v.validate(1))
    }

    @Test
    fun `nonNegative Int`() {
        val v = Validator<Int>(shortCircuit = false) { nonNegative() }
        assertEquals(ValidationResult.Valid, v.validate(0))
        assertEquals(ValidationResult.Valid, v.validate(1))
        assertIs<ValidationResult.Invalid>(v.validate(-1))
    }

    @Test
    fun `nonPositive Int`() {
        val v = Validator<Int>(shortCircuit = false) { nonPositive() }
        assertEquals(ValidationResult.Valid, v.validate(0))
        assertEquals(ValidationResult.Valid, v.validate(-1))
        assertIs<ValidationResult.Invalid>(v.validate(1))
    }

    @Test
    fun `matches enforces regex`() {
        val v =
            Validator<String>(shortCircuit = false) {
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

    @Test
    fun `minSize enforces lower bound`() {
        val v = Validator<List<Int>>(shortCircuit = false) { minSize(2) }
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2)))
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2, 3)))
        assertIs<ValidationResult.Invalid>(v.validate(listOf(1)))
    }

    @Test
    fun `unique rejects duplicates`() {
        val v = Validator<List<Int>>(shortCircuit = false) { unique() }
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2, 3)))
        assertEquals(ValidationResult.Valid, v.validate(emptyList<Int>()))
        assertIs<ValidationResult.Invalid>(v.validate(listOf(1, 2, 1)))
    }

    @Test
    fun `includesAll requires all values`() {
        val v = Validator<List<Int>>(shortCircuit = false) { includesAll(listOf(1, 2)) }
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2, 3)))
        assertEquals(ValidationResult.Valid, v.validate(listOf(1, 2)))
        assertIs<ValidationResult.Invalid>(v.validate(listOf(1)))
    }

    @Test
    fun `noneOf rejects forbidden`() {
        val v = Validator<String>(shortCircuit = false) { noneOf(listOf("admin", "root")) }
        assertEquals(ValidationResult.Valid, v.validate("alice"))
        assertIs<ValidationResult.Invalid>(v.validate("admin"))
    }

    @Test
    fun `isTrue accepts true only`() {
        val v = Validator<Boolean>(shortCircuit = false) { isTrue() }
        assertEquals(ValidationResult.Valid, v.validate(true))
        assertIs<ValidationResult.Invalid>(v.validate(false))
    }

    @Test
    fun `isFalse accepts false only`() {
        val v = Validator<Boolean>(shortCircuit = false) { isFalse() }
        assertEquals(ValidationResult.Valid, v.validate(false))
        assertIs<ValidationResult.Invalid>(v.validate(true))
    }

    data class Signup(
        val password: String,
        val confirm: String,
    )

    @Test
    fun `requireThat enables cross-field check`() {
        val v =
            Validator<Signup>(shortCircuit = false) {
                requireThat(password == confirm) { Message.Unsafe("passwords must match") }
            }
        assertEquals(ValidationResult.Valid, v.validate(Signup("abc", "abc")))
        val r = v.validate(Signup("abc", "xyz"))
        assertIs<ValidationResult.Invalid>(r)
        assertEquals(
            "passwords must match",
            r.errors
                .single()
                .message.text,
        )
    }

    @Test
    fun `anyOf passes when any rule passes`() {
        val v =
            Validator<String>(shortCircuit = false) {
                anyOf(
                    { hasPrefix("foo") },
                    { hasPrefix("bar") },
                    message = { Message.Unsafe("must start with foo or bar") },
                )
            }
        assertEquals(ValidationResult.Valid, v.validate("foo123"))
        assertEquals(ValidationResult.Valid, v.validate("bar123"))
        val r = v.validate("baz")
        assertIs<ValidationResult.Invalid>(r)
        assertEquals(
            "must start with foo or bar",
            r.errors
                .single()
                .message.text,
        )
    }

    @Test
    fun `not inverts a rule`() {
        val v =
            Validator<String>(shortCircuit = false) {
                not({ hasPrefix("admin_") }, message = { Message.Unsafe("must not be reserved") })
            }
        assertEquals(ValidationResult.Valid, v.validate("alice"))
        assertIs<ValidationResult.Invalid>(v.validate("admin_root"))
    }

    @Test
    fun `notEmpty Map rejects empty`() {
        val v = Validator<Map<String, Int>>(shortCircuit = false) { notEmpty() }
        assertEquals(ValidationResult.Valid, v.validate(mapOf("a" to 1)))
        assertIs<ValidationResult.Invalid>(v.validate(emptyMap<String, Int>()))
    }

    data class Cfg(
        val flags: Map<String, String>,
    )

    @Test
    fun `eachEntry validates per entry with bracket path`() {
        val v =
            Validator<Cfg> {
                field(::flags) {
                    eachEntry { _, _ -> notBlank() }
                }
            }
        assertEquals(ValidationResult.Valid, v.validate(Cfg(mapOf("a" to "x", "b" to "y"))))
        val r = v.validate(Cfg(mapOf("a" to "x", "b" to "")))
        assertIs<ValidationResult.Invalid>(r)
        val err = r.errors.single()
        assertIs<ValidationError.Field>(err)
        assertEquals("flags[b]", err.path)
    }

    @Test
    fun `eachKey validates keys`() {
        val v =
            Validator<Map<String, Int>>(shortCircuit = false) {
                eachKey { notBlank() }
            }
        assertEquals(ValidationResult.Valid, v.validate(mapOf("a" to 1)))
        assertIs<ValidationResult.Invalid>(v.validate(mapOf("" to 1)))
    }

    @Test
    fun `eachValue validates values`() {
        val v =
            Validator<Map<String, String>>(shortCircuit = false) {
                eachValue { notBlank() }
            }
        assertEquals(ValidationResult.Valid, v.validate(mapOf("a" to "x")))
        val r = v.validate(mapOf("a" to ""))
        assertIs<ValidationResult.Invalid>(r)
        val err = r.errors.single()
        assertIs<ValidationError.Field>(err)
        assertEquals("[a]", err.path)
    }

    data class Holder(
        val items: List<String>,
    )

    @Test
    fun `eachItem reports indexed errors`() {
        val v =
            Validator<Holder> {
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

    data class Wrapper(
        val inner: String?,
    )

    @Test
    fun `optional skips null`() {
        val v =
            Validator<Wrapper> {
                optional(::inner) { notBlank() }
            }
        assertEquals(ValidationResult.Valid, v.validate(Wrapper(null)))
        assertIs<ValidationResult.Invalid>(v.validate(Wrapper("")))
    }

    data class Outer(
        val name: String,
    )

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

    @Test
    fun `validated accepts supertype Validator via in-projection`() {
        val anyV: Validator<Any> = Validator { /* always valid */ }
        val outerV = Validator<Outer> { validated(::name, anyV) }
        assertEquals(ValidationResult.Valid, outerV.validate(Outer("anything")))
    }
}
