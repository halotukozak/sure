package halotukozak.validation

import halotukozak.validation.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ValidatorTest {

    data class User(val name: String, val age: Int)

    private val nameValidator = Validator<String> { notBlank(); lengthIn(1..50) }

    private val userValidator = Validator<User> {
        field(::name) { notBlank(); lengthIn(1..50) }
        field(::age) { inRange(0..150) }
    }

    @Test
    fun `valid string passes`() {
        assertEquals(ValidationResult.Valid, nameValidator.validate("alice"))
    }

    @Test
    fun `null fails with NotNull`() {
        val result = nameValidator.validate(null)
        assertIs<ValidationResult.Invalid>(result)
        assertEquals(Message.NotNull, result.errors.single().message)
    }

    @Test
    fun `wrong type fails with TypeMismatched`() {
        val result = nameValidator.validate(42)
        assertIs<ValidationResult.Invalid>(result)
        assertEquals(Message.TypeMismatched(String::class, Int::class), result.errors.single().message)
    }

    @Test
    fun `blank string fails with NotBlank`() {
        val result = nameValidator.validate("   ")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals(Message.NotBlank, result.errors.single().message)
    }

    @Test
    fun `failFast stops on first error`() {
        val v = Validator<String>(shortCircuit = true) {
            notBlank()
            lengthIn(10..20)
        }
        val result = v.validate("")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals(1, result.errors.size)
        assertEquals(Message.NotBlank, result.errors.single().message)
    }

    @Test
    fun `accumulating collects all errors`() {
        val v = Validator<String>(shortCircuit = false) {
            notBlank()
            lengthIn(10..20)
        }.accumulating()
        val result = v.validate("ab")
        assertIs<ValidationResult.Invalid>(result)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun `field path includes field name`() {
        val result = userValidator.validate(User("", 30))
        assertIs<ValidationResult.Invalid>(result)
        val err = result.errors.single()
        assertIs<ValidationError.Field>(err)
        assertEquals("name", err.path)
    }

    @Test
    fun `nullable validator passes for null`() {
        assertEquals(ValidationResult.Valid, nameValidator.nullable().validate(null))
    }

    @Test
    fun `nullable validator delegates for non-null`() {
        val result = nameValidator.nullable().validate("")
        assertIs<ValidationResult.Invalid>(result)
    }

    @Test
    fun `empty validator always valid`() {
        assertEquals(ValidationResult.Valid, Validator.empty<String>().validate("anything"))
        assertEquals(ValidationResult.Valid, Validator.empty<User>().validate(User("", -1)))
    }
}
