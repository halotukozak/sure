package halotukozak.validation

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@DslMarker
annotation class ValidationDsl

@ValidationDsl
sealed class ValidationScope<out T>(
    internal val path: String,
    @PublishedApi
    internal val shortCircuit: Boolean,
) {
    abstract fun raise(message: Message)

    internal abstract fun report(error: ValidationError)

    /** Reports [error] and, when fail-fast, unwinds to the nearest runScope { } so siblings stop running. */
    protected fun fail(error: ValidationError) {
        report(error)
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal class RootScope<out T>(
    val value: T,
    shortCircuit: Boolean,
) : ValidationScope<T>("", shortCircuit) {
    private val _errors = mutableListOf<ValidationError>()
    val errors: List<ValidationError> get() = _errors

    override fun report(error: ValidationError) {
        _errors += error
    }

    override fun raise(message: Message) = fail(ValidationError.Root(message))
}

@PublishedApi
internal class FieldScope<out T>(
    val value: T,
    name: String,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>(if (parent.path.isEmpty()) name else "${parent.path}.$name", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) = fail(ValidationError.Field(path, message))
}

@PublishedApi
internal class ItemScope<out T>(
    val value: T,
    private val index: Int,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$index]", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) = fail(ValidationError.Element(parent.path, index, message))
}

@PublishedApi
internal class EntryScope<out T>(
    val value: T,
    key: Any?,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$key]", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) = fail(ValidationError.Field(path, message))
}

@PublishedApi
internal class EphemeralScope<out T>(
    val value: T,
    parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>(parent.path, shortCircuit) {
    private val _errors = mutableListOf<ValidationError>()
    val errors: List<ValidationError> get() = _errors

    override fun report(error: ValidationError) {
        _errors += error
    }

    override fun raise(message: Message) = fail(ValidationError.Root(message))
}

// Kept as an extension (not a base-class val) so the contract below can smart-cast the receiver
@OptIn(ExperimentalContracts::class)
val <T> ValidationScope<T>.value: T
    get() {
        contract {
            returnsNotNull() implies (this@value is ValidationScope<T & Any>)
        }

        return when (this) {
            is RootScope -> value
            is FieldScope -> value
            is ItemScope -> value
            is EntryScope -> value
            is EphemeralScope -> value
        }
    }
