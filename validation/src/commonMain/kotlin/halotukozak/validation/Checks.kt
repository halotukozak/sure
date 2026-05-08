@file:Suppress("unused")

package halotukozak.validation

import halotukozak.validation.ValidationError.Message
import kotlin.reflect.KProperty0

context(scope: ValidationScope<T>)
inline fun <T> check(predicate: (T) -> Boolean, onError: (T) -> Message) {
    if (predicate(scope.value)) scope.raise(onError(scope.value))
}

context(_: ValidationScope<String>)
fun notBlank() = check({ it.isBlank() }) { Message.NotBlank }

context(_: ValidationScope<String>)
fun lengthIn(range: IntRange) = check({ it.length !in range }) { Message.LengthIn(range) }

context(_: ValidationScope<String>)
fun maxLength(n: Int) = check({ it.length > n }) { Message.AtMostCharacters(n) }

context(_: ValidationScope<String>)
fun matches(regex: Regex, message: Message) = check({ !regex.matches(it) }) { message }

context(_: ValidationScope<String>)
fun noLeadingOrTrailingSpace() = check({ it.startsWith(' ') || it.endsWith(' ') }) {
    Message.LeadingOrTrailingSpace
}

context(_: ValidationScope<String>)
fun noConsecutiveSpaces() = check({ "  " in it }) { Message.ConsecutiveSpaces }

context(_: ValidationScope<T>)
fun <T : Comparable<T>> inRange(range: ClosedRange<T>) = check({ it !in range }) {
    Message.InRange(range)
}

context(_: ValidationScope<T>)
fun <T : Any> oneOf(values: Collection<T>) = check({ it !in values }) { Message.OneOf(values) }

context(_: ValidationScope<List<T>>)
fun <T : Any> notEmpty() = check<List<T>>({ it.isEmpty() }) { Message.NotEmpty }

context(_: ValidationScope<List<T>>)
fun <T : Any> maxSize(n: Int) = check<List<T>>({ it.size > n }) { Message.AtMostItems(n) }

context(_: ValidationScope<List<T>>)
fun <T : Any> sizeIn(range: IntRange) = check<List<T>>({ it.size !in range }) { Message.InRange(range) }

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
inline fun <T : Any> eachItem(shortCircuit: Boolean = scope.shortCircuit, rule: ValidationScope<T>.(T) -> Unit) {
    scope.value.forEachIndexed { index, item ->
        val itemScope = ItemScope(item, index, scope, shortCircuit)
        runScope { itemScope.rule(item) }
    }
}

context(scope: ValidationScope<*>)
fun <T> validated(property: KProperty0<T>, with: Validator<T>) {
    val fieldScope = FieldScope(property.get(), property.name, scope, with.shortCircuit)
    runScope { with.applyRules(fieldScope) }
}
