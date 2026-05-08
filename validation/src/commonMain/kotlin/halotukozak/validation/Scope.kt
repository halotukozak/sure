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
}

@PublishedApi
internal class RootScope<out T>(val value: T, shortCircuit: Boolean) : ValidationScope<T>("", shortCircuit) {
    private val _errors = mutableListOf<ValidationError>()
    val errors: List<ValidationError> get() = _errors

    override fun report(error: ValidationError) {
        _errors += error
    }

    override fun raise(message: Message) {
        report(ValidationError.Root(message))
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal class FieldScope<out T>(
    val value: T,
    name: String,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>(if (parent.path.isEmpty()) name else "${parent.path}.$name", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) {
        report(ValidationError.Field(path, message))
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal class ItemScope<out T>(
    val value: T,
    private val index: Int,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$index]", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) {
        report(ValidationError.Element(parent.path, index, message))
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal class EntryScope<out T>(
    val value: T,
    key: Any?,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$key]", shortCircuit) {
    override fun report(error: ValidationError) = parent.report(error)

    override fun raise(message: Message) {
        report(ValidationError.Field(path, message))
        if (shortCircuit) throw ScopeShortCircuit()
    }
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

    override fun raise(message: Message) {
        _errors += ValidationError.Root(message)
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

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
