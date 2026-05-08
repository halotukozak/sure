package halotukozak.validation

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Suppress("FunctionName")
@Serializable(with = Message.Serializer::class)
open class Message(val text: String) {
    override fun equals(other: Any?): Boolean = this === other || (other is Message && text == other.text)

    override fun hashCode(): Int = text.hashCode()

    override fun toString(): String = text

    object Serializer : KSerializer<Message> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Message", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Message) = encoder.encodeString(value.text)

        override fun deserialize(decoder: Decoder): Message = Message(decoder.decodeString())
    }

    object NotBlank : Message("must not be blank")

    object NotNull : Message("must not be null")

    object NotEmpty : Message("must not be empty")

    object LeadingOrTrailingSpace : Message("must not have leading or trailing spaces")

    object ConsecutiveSpaces : Message("must not contain consecutive spaces")

    object Positive : Message("must be positive")

    object Negative : Message("must be negative")

    object NonNegative : Message("must not be negative")

    object NonPositive : Message("must not be positive")

    object Unique : Message("must contain unique items")

    object IsTrue : Message("must be true")

    object IsFalse : Message("must be false")

    class InRange private constructor(text: String) : Message("must be in $text") {
        constructor(range: IntRange) : this(range.toString())
        constructor(range: ClosedRange<*>) : this(range.toString())
    }

    class LengthIn(range: IntRange) : Message("must be $range characters")

    class AtMostCharacters(n: Int) : Message("must be at most $n characters")

    class AtLeastCharacters(n: Int) : Message("must be at least $n characters")

    class AtMostItems(n: Int) : Message("must contain at most $n items")

    class AtLeastItems(n: Int) : Message("must contain at least $n items")

    class AtMost(value: Any?) : Message("must be at most $value")

    class AtLeast(value: Any?) : Message("must be at least $value")

    class StartsWith(prefix: String) : Message("must start with $prefix")

    class EndsWith(suffix: String) : Message("must end with $suffix")

    class Contains(substring: String) : Message("must contain $substring")

    class ContainsAll(values: Collection<*>) : Message("must contain all of ${values.joinToString()}")

    class OneOf(values: Collection<*>) : Message("must be one of ${values.joinToString()}")

    class NoneOf(values: Collection<*>) : Message("must not be one of ${values.joinToString()}")

    class Matches(pattern: String) : Message("must match $pattern")

    class TypeMismatched(expected: KClass<*>, actual: KClass<*>) :
        Message("expected type ${expected.simpleName}, but got ${actual.simpleName}")

    class Unsafe(text: String) : Message(text)
}
