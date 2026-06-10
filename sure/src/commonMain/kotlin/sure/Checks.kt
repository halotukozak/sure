@file:Suppress("unused")

package sure

import sure.Message
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty0

context(scope: ValidationScope<T>)
inline fun <T> check(
    predicate: (T) -> Boolean,
    onError: (T) -> Message,
) {
    if (predicate(scope.value)) scope.raise(onError(scope.value))
}

context(scope: ValidationScope<*>)
inline fun requireThat(
    condition: Boolean,
    message: () -> Message,
) {
    if (!condition) scope.raise(message())
}

context(scope: ValidationScope<T>)
fun <T> anyOf(
    vararg rules: ValidationScope<T>.() -> Unit,
    message: () -> Message,
) {
    val v = scope.value
    val anyValid =
        rules.any { rule ->
            val isolated = EphemeralScope(v, scope)
            runScope { isolated.rule() }
            isolated.errors.isEmpty()
        }
    if (!anyValid) scope.raise(message())
}

context(scope: ValidationScope<T>)
fun <T> not(
    rule: ValidationScope<T>.() -> Unit,
    message: () -> Message,
) {
    val isolated = EphemeralScope(scope.value, scope)
    runScope { isolated.rule() }
    if (isolated.errors.isEmpty()) scope.raise(message())
}

context(_: ValidationScope<String>)
fun notBlank() = check({ it.isBlank() }) { Message.NotBlank }

@JvmName("notEmptyString")
context(_: ValidationScope<String>)
fun notEmpty() = check({ it.isEmpty() }) { Message.NotEmpty }

context(_: ValidationScope<String>)
fun hasPrefix(prefix: String) = check({ !it.startsWith(prefix) }) { Message.StartsWith(prefix) }

context(_: ValidationScope<String>)
fun hasSuffix(suffix: String) = check({ !it.endsWith(suffix) }) { Message.EndsWith(suffix) }

context(_: ValidationScope<String>)
fun includes(substring: String) = check({ substring !in it }) { Message.Contains(substring) }

context(_: ValidationScope<String>)
fun lengthIn(range: IntRange) = check({ it.length !in range }) { Message.LengthIn(range) }

context(_: ValidationScope<String>)
fun maxLength(n: Int) = check({ it.length > n }) { Message.AtMostCharacters(n) }

context(_: ValidationScope<String>)
fun minLength(n: Int) = check({ it.length < n }) { Message.AtLeastCharacters(n) }

context(_: ValidationScope<String>)
fun matches(
    regex: Regex,
    message: Message,
) = check({ !regex.matches(it) }) { message }

context(_: ValidationScope<String>)
fun noLeadingOrTrailingSpace() =
    check({ it.startsWith(' ') || it.endsWith(' ') }) {
        Message.LeadingOrTrailingSpace
    }

context(_: ValidationScope<String>)
fun noConsecutiveSpaces() = check({ "  " in it }) { Message.ConsecutiveSpaces }

context(_: ValidationScope<T>)
fun <T : Comparable<T>> inRange(range: ClosedRange<T>) = check({ it !in range }) { Message.InRange(range) }

context(_: ValidationScope<T>)
fun <T : Comparable<T>> min(value: T) = check({ it < value }) { Message.AtLeast(value) }

context(_: ValidationScope<T>)
fun <T : Comparable<T>> max(value: T) = check({ it > value }) { Message.AtMost(value) }

@JvmName("positiveInt")
context(_: ValidationScope<Int>)
fun positive() = check({ it <= 0 }) { Message.Positive }

@JvmName("positiveLong")
context(_: ValidationScope<Long>)
fun positive() = check({ it <= 0L }) { Message.Positive }

@JvmName("positiveDouble")
context(_: ValidationScope<Double>)
fun positive() = check({ it <= 0.0 }) { Message.Positive }

@JvmName("negativeInt")
context(_: ValidationScope<Int>)
fun negative() = check({ it >= 0 }) { Message.Negative }

@JvmName("negativeLong")
context(_: ValidationScope<Long>)
fun negative() = check({ it >= 0L }) { Message.Negative }

@JvmName("negativeDouble")
context(_: ValidationScope<Double>)
fun negative() = check({ it >= 0.0 }) { Message.Negative }

@JvmName("nonNegativeInt")
context(_: ValidationScope<Int>)
fun nonNegative() = check({ it < 0 }) { Message.NonNegative }

@JvmName("nonNegativeLong")
context(_: ValidationScope<Long>)
fun nonNegative() = check({ it < 0L }) { Message.NonNegative }

@JvmName("nonNegativeDouble")
context(_: ValidationScope<Double>)
fun nonNegative() = check({ it < 0.0 }) { Message.NonNegative }

@JvmName("nonPositiveInt")
context(_: ValidationScope<Int>)
fun nonPositive() = check({ it > 0 }) { Message.NonPositive }

@JvmName("nonPositiveLong")
context(_: ValidationScope<Long>)
fun nonPositive() = check({ it > 0L }) { Message.NonPositive }

@JvmName("nonPositiveDouble")
context(_: ValidationScope<Double>)
fun nonPositive() = check({ it > 0.0 }) { Message.NonPositive }

context(_: ValidationScope<T>)
fun <T : Any> oneOf(values: Collection<T>) = check({ it !in values }) { Message.OneOf(values) }

context(_: ValidationScope<T>)
fun <T : Any> noneOf(values: Collection<T>) = check({ it in values }) { Message.NoneOf(values) }

context(_: ValidationScope<List<T>>)
fun <T : Any> notEmpty() = check<List<T>>({ it.isEmpty() }) { Message.NotEmpty }

context(_: ValidationScope<List<T>>)
fun <T : Any> maxSize(n: Int) = check<List<T>>({ it.size > n }) { Message.AtMostItems(n) }

context(_: ValidationScope<List<T>>)
fun <T : Any> minSize(n: Int) = check<List<T>>({ it.size < n }) { Message.AtLeastItems(n) }

context(_: ValidationScope<List<T>>)
fun <T : Any> sizeIn(range: IntRange) = check<List<T>>({ it.size !in range }) { Message.InRange(range) }

context(_: ValidationScope<List<T>>)
fun <T : Any> unique() = check<List<T>>({ it.size != it.toSet().size }) { Message.Unique }

context(_: ValidationScope<List<T>>)
fun <T : Any> includesAll(values: Collection<T>) = check<List<T>>({ !it.containsAll(values) }) { Message.ContainsAll(values) }

@JvmName("notEmptyMap")
context(_: ValidationScope<Map<K, V>>)
fun <K, V> notEmpty() = check<Map<K, V>>({ it.isEmpty() }) { Message.NotEmpty }

context(scope: ValidationScope<Map<K, V>>)
inline fun <K, V : Any> eachEntry(
    shortCircuit: Boolean = scope.shortCircuit,
    rule: ValidationScope<V>.(K, V) -> Unit,
) {
    for ((k, v) in scope.value) {
        val entryScope = EntryScope(v, k, scope, shortCircuit)
        runScope { entryScope.rule(k, v) }
    }
}

context(scope: ValidationScope<Map<K, V>>)
inline fun <K : Any, V> eachKey(
    shortCircuit: Boolean = scope.shortCircuit,
    rule: ValidationScope<K>.(K) -> Unit,
) {
    for (k in scope.value.keys) {
        val entryScope = EntryScope(k, k, scope, shortCircuit)
        runScope { entryScope.rule(k) }
    }
}

context(scope: ValidationScope<Map<K, V>>)
inline fun <K, V : Any> eachValue(
    shortCircuit: Boolean = scope.shortCircuit,
    rule: ValidationScope<V>.(V) -> Unit,
) {
    for ((k, v) in scope.value) {
        val entryScope = EntryScope(v, k, scope, shortCircuit)
        runScope { entryScope.rule(v) }
    }
}

context(_: ValidationScope<Boolean>)
fun isTrue() = check({ !it }) { Message.IsTrue }

context(_: ValidationScope<Boolean>)
fun isFalse() = check({ it }) { Message.IsFalse }

context(scope: ValidationScope<*>)
inline fun <F : Any> field(
    property: KProperty0<F>,
    shortCircuit: Boolean = scope.shortCircuit,
    block: ValidationScope<F>.(F) -> Unit = {},
) {
    val value = property.get()
    val fieldScope = FieldScope(value, property.name, scope, shortCircuit)
    runScope { fieldScope.block(value) }
}

context(scope: ValidationScope<*>)
inline fun <F : Any> optional(
    property: KProperty0<F?>,
    shortCircuit: Boolean = scope.shortCircuit,
    block: ValidationScope<F>.(F) -> Unit,
) {
    val value = property.get()
    if (value != null) {
        val fieldScope = FieldScope(value, property.name, scope, shortCircuit)
        runScope { fieldScope.block(value) }
    }
}

context(scope: ValidationScope<List<T>>)
inline fun <T : Any> eachItem(
    shortCircuit: Boolean = scope.shortCircuit,
    rule: ValidationScope<T>.(T) -> Unit,
) {
    scope.value.forEachIndexed { index, item ->
        val itemScope = ItemScope(item, index, scope, shortCircuit)
        runScope { itemScope.rule(item) }
    }
}

context(scope: ValidationScope<*>)
fun <T : Any> validated(
    property: KProperty0<T>,
    with: Validator<T>,
    shortCircuit: Boolean = scope.shortCircuit,
) {
    val fieldScope = FieldScope(property.get(), property.name, scope, shortCircuit)
    runScope { with.applyRules(fieldScope) }
}

context(scope: ValidationScope<*>)
fun <T : Any> validatedOptional(
    property: KProperty0<T?>,
    with: Validator<T>,
    shortCircuit: Boolean = scope.shortCircuit,
) {
    val value = property.get()
    if (value != null) {
        val fieldScope = FieldScope(value, property.name, scope, shortCircuit)
        runScope { with.applyRules(fieldScope) }
    }
}
