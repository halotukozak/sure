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

            fun AtLeastCharacters(n: Int) = Message("must be at least $n characters")

            fun AtMostItems(n: Int) = Message("must contain at most $n items")

            fun AtLeastItems(n: Int) = Message("must contain at least $n items")

            fun AtMost(value: Any?) = Message("must be at most $value")

            fun AtLeast(value: Any?) = Message("must be at least $value")

            val Positive = Message("must be positive")

            val Negative = Message("must be negative")

            val NonNegative = Message("must not be negative")

            val NonPositive = Message("must not be positive")

            fun StartsWith(prefix: String) = Message("must start with $prefix")

            fun EndsWith(suffix: String) = Message("must end with $suffix")

            fun Contains(substring: String) = Message("must contain $substring")

            val Unique = Message("must contain unique items")

            fun ContainsAll(values: Collection<*>) = Message("must contain all of ${values.joinToString()}")

            fun NoneOf(values: Collection<*>) = Message("must not be one of ${values.joinToString()}")

            val IsTrue = Message("must be true")

            val IsFalse = Message("must be false")

            fun OneOf(values: Collection<*>) = Message("must be one of ${values.joinToString()}")

            fun Matches(pattern: String) = Message("must match $pattern")

            fun TypeMismatched(expected: KClass<*>, actual: KClass<*>) =
                Message("expected type ${expected.simpleName}, but got ${actual.simpleName}")

            fun Unsafe(text: String) = Message(text)
        }
    }
}
