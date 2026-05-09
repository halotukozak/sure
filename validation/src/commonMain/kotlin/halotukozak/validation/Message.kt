package halotukozak.validation

import kotlin.reflect.KClass

@Suppress("FunctionName")
data class Message(
    /** Stable identifier for translation lookup, e.g. `"validation.lengthIn"`. */
    val key: String,
    /** Arguments referenced by the translation template, pre-stringified for transport safety. */
    val args: List<String> = emptyList(),
    /** Default English rendering, used as fallback when no translator resolves [key]. */
    val text: String = key,
) {
    /**
     * Renders the message. Calls [translator] with [key] and [args]; falls back to [text] when
     * the translator returns null or no translator is supplied.
     */
    fun render(translator: Translator? = null): String =
        translator?.translate(key, args) ?: text

    override fun toString(): String = text

    companion object {
        val NotBlank = Message("validation.notBlank", text = "must not be blank")

        val NotNull = Message("validation.notNull", text = "must not be null")

        val NotEmpty = Message("validation.notEmpty", text = "must not be empty")

        val LeadingOrTrailingSpace =
            Message("validation.noLeadingOrTrailingSpace", text = "must not have leading or trailing spaces")

        val ConsecutiveSpaces =
            Message("validation.noConsecutiveSpaces", text = "must not contain consecutive spaces")

        val Positive = Message("validation.positive", text = "must be positive")

        val Negative = Message("validation.negative", text = "must be negative")

        val NonNegative = Message("validation.nonNegative", text = "must not be negative")

        val NonPositive = Message("validation.nonPositive", text = "must not be positive")

        val Unique = Message("validation.unique", text = "must contain unique items")

        val IsTrue = Message("validation.isTrue", text = "must be true")

        val IsFalse = Message("validation.isFalse", text = "must be false")

        fun InRange(range: IntRange) = inRange(range.toString())

        fun InRange(range: ClosedRange<*>) = inRange(range.toString())

        private fun inRange(rangeRepr: String) = Message("validation.inRange", listOf(rangeRepr), "must be in $rangeRepr")

        fun LengthIn(range: IntRange) = Message("validation.lengthIn", listOf(range.toString()), "must be $range characters")

        fun AtMostCharacters(n: Int) = Message("validation.atMostCharacters", listOf(n.toString()), "must be at most $n characters")

        fun AtLeastCharacters(n: Int) = Message("validation.atLeastCharacters", listOf(n.toString()), "must be at least $n characters")

        fun AtMostItems(n: Int) = Message("validation.atMostItems", listOf(n.toString()), "must contain at most $n items")

        fun AtLeastItems(n: Int) = Message("validation.atLeastItems", listOf(n.toString()), "must contain at least $n items")

        fun AtMost(value: Any?) = Message("validation.atMost", listOf(value.toString()), "must be at most $value")

        fun AtLeast(value: Any?) = Message("validation.atLeast", listOf(value.toString()), "must be at least $value")

        fun StartsWith(prefix: String) = Message("validation.startsWith", listOf(prefix), "must start with $prefix")

        fun EndsWith(suffix: String) = Message("validation.endsWith", listOf(suffix), "must end with $suffix")

        fun Contains(substring: String) = Message("validation.contains", listOf(substring), "must contain $substring")

        fun ContainsAll(values: Collection<*>) =
            Message(
                "validation.containsAll",
                values.map { it.toString() },
                "must contain all of ${values.joinToString()}",
            )

        fun OneOf(values: Collection<*>) =
            Message(
                "validation.oneOf",
                values.map { it.toString() },
                "must be one of ${values.joinToString()}",
            )

        fun NoneOf(values: Collection<*>) =
            Message(
                "validation.noneOf",
                values.map { it.toString() },
                "must not be one of ${values.joinToString()}",
            )

        fun Matches(pattern: String) = Message("validation.matches", listOf(pattern), "must match $pattern")

        fun TypeMismatched(
            expected: KClass<*>,
            actual: KClass<*>,
        ) = Message(
            "validation.typeMismatched",
            listOf(expected.simpleName.orEmpty(), actual.simpleName.orEmpty()),
            "expected type ${expected.simpleName}, but got ${actual.simpleName}",
        )

        /** Escape hatch — a free-text message with no translation key. The text doubles as the key. */
        fun Unsafe(text: String) = Message(key = text, text = text)
    }
}

fun interface Translator {
    /** Returns the localized rendering for [key] / [args], or `null` when not in catalogue. */
    fun translate(
        key: String,
        args: List<String>,
    ): String?
}
