package halotukozak.validation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@Serializable
sealed interface ValidationError {
    val message: Message

    @Serializable
    @SerialName("field")
    data class Field(val path: String, override val message: Message) : ValidationError {
        override fun toString(): String = "$path: $message"
    }

    @Serializable
    @SerialName("element")
    data class Element(
        val path: String,
        val index: Int,
        override val message: Message,
    ) : ValidationError {
        override fun toString(): String = "$path[$index]: $message"
    }

    @Serializable
    @SerialName("root")
    data class Root(override val message: Message) : ValidationError {
        override fun toString(): String = message.toString()
    }

    @Suppress("FunctionName")
    @Serializable
    @JvmInline
    value class Message private constructor(val message: String) {
        companion object {
            val NotBlank = Message("must not be blank")

            val NotNull = Message("must not be null")

            val NotEmpty = Message("must not be empty")

            val LeadingOrTrailingSpace = Message("must not have leading or trailing spaces")

            val ConsecutiveSpaces = Message("must not contain consecutive spaces")

            private fun InRange(rangeRepr: String) = Message("must be in $rangeRepr")

            fun InRange(range: IntRange) = InRange(range.toString())

            fun InRange(range: ClosedRange<*>) = InRange(range.toString())

            fun LengthIn(range: IntRange) = Message("must be $range characters")

            fun AtMostCharacters(n: Int) = Message("must be at most $n characters")

            fun AtMostItems(n: Int) = Message("must contain at most $n items")

            fun OneOf(values: Collection<*>) = Message("must be one of ${values.joinToString()}")

            fun Matches(pattern: String) = Message("must match $pattern")

            fun TypeMismatched(expected: KClass<*>, actual: KClass<*>) =
                Message("expected type ${expected.simpleName}, but got ${actual.simpleName}")

            fun Unsafe(text: String) = Message(text)
        }
    }
}
