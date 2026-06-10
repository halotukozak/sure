package sure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MessageTest {
    @Test
    fun `messages with same key args and text are equal`() {
        val a = Message("validation.notBlank", text = "must not be blank")
        val b = Message("validation.notBlank", text = "must not be blank")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `messages with different keys are not equal`() {
        assertNotEquals(Message.NotBlank, Message.NotEmpty)
    }

    @Test
    fun `messages with different args are not equal`() {
        assertNotEquals(Message.LengthIn(1..5), Message.LengthIn(1..6))
    }

    @Test
    fun `messages with different text are not equal`() {
        assertNotEquals(
            Message("validation.notBlank", text = "must not be blank"),
            Message("validation.notBlank", text = "translated"),
        )
    }

    @Test
    fun `catalog member equals freshly constructed Message with matching fields`() {
        assertEquals(
            Message.NotBlank,
            Message("validation.notBlank", text = "must not be blank"),
        )
    }

    @Test
    fun `toString returns text`() {
        assertEquals("must not be blank", Message.NotBlank.toString())
        assertEquals("must be 1..5 characters", Message.LengthIn(1..5).toString())
    }

    @Test
    fun `render without translator falls back to text`() {
        assertEquals("must not be blank", Message.NotBlank.render())
    }

    @Test
    fun `render with translator returning null falls back to text`() {
        val translator = Translator { _, _ -> null }
        assertEquals("must not be blank", Message.NotBlank.render(translator))
    }

    @Test
    fun `render with translator returning value uses translation`() {
        val translator =
            Translator { key, _ ->
                when (key) {
                    "validation.notBlank" -> "nie moze byc puste"
                    else -> null
                }
            }
        assertEquals("nie moze byc puste", Message.NotBlank.render(translator))
    }

    @Test
    fun `render passes args to translator`() {
        var captured: List<String>? = null
        val translator =
            Translator { _, args ->
                captured = args
                "ok"
            }
        Message.LengthIn(1..5).render(translator)
        assertEquals(listOf("1..5"), captured)
    }

    @Test
    fun `factory args are stringified`() {
        val msg = Message.AtMost(42)
        assertEquals(listOf("42"), msg.args)
        assertEquals("validation.atMost", msg.key)
        assertEquals("must be at most 42", msg.text)
    }

    @Test
    fun `OneOf stringifies collection elements`() {
        val msg = Message.OneOf(listOf("a", "b"))
        assertEquals(listOf("a", "b"), msg.args)
    }

    @Test
    fun `Unsafe uses text as key`() {
        val msg = Message.Unsafe("custom error")
        assertEquals("custom error", msg.key)
        assertEquals("custom error", msg.text)
        assertEquals(emptyList(), msg.args)
    }

    @Test
    fun `user can extend with custom Message via constructor`() {
        val custom = Message("myapp.email", text = "invalid email")
        assertEquals("myapp.email", custom.key)
        assertEquals("invalid email", custom.text)
        assertEquals(custom, Message("myapp.email", text = "invalid email"))
    }

    @Test
    fun `data class copy enables tweaking text without rebuilding`() {
        val translated = Message.NotBlank.copy(text = "nie moze byc puste")
        assertEquals("nie moze byc puste", translated.text)
        assertEquals(Message.NotBlank.key, translated.key)
        assertEquals(Message.NotBlank.args, translated.args)
    }
}
