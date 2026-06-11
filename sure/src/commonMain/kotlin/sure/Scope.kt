package sure

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
    internal abstract fun addError(error: ValidationError)

    abstract fun raise(message: Message)

    /** Reports [error] and, when fail-fast, unwinds to the nearest runScope { } so siblings stop running. */
    protected fun raise(error: ValidationError) {
        addError(error)
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal class RootScope<out T>(
    val value: T,
    shortCircuit: Boolean,
) : ValidationScope<T>("", shortCircuit) {
    val errors: List<ValidationError>
        field = mutableListOf<ValidationError>()

    override fun addError(error: ValidationError) {
        errors += error
    }

    override fun raise(message: Message) = raise(ValidationError.Root(message))
}

@PublishedApi
internal class FieldScope<out T>(
    val value: T,
    name: String,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>(if (parent.path.isEmpty()) name else "${parent.path}.$name", shortCircuit) {
    override fun addError(error: ValidationError) = parent.addError(error)

    override fun raise(message: Message) = raise(ValidationError.Field(path, message))
}

@PublishedApi
internal class ItemScope<out T>(
    val value: T,
    private val index: Int,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$index]", shortCircuit) {
    override fun addError(error: ValidationError) = parent.addError(error)

    override fun raise(message: Message) = raise(ValidationError.Element(parent.path, index, message))
}

@PublishedApi
internal class EntryScope<out T>(
    val value: T,
    key: Any?,
    private val parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>("${parent.path}[$key]", shortCircuit) {
    override fun addError(error: ValidationError) = parent.addError(error)

    override fun raise(message: Message) = raise(ValidationError.Field(path, message))
}

@PublishedApi
internal class EphemeralScope<out T>(
    val value: T,
    parent: ValidationScope<*>,
    shortCircuit: Boolean = parent.shortCircuit,
) : ValidationScope<T>(parent.path, shortCircuit) {
    val errors: List<ValidationError>
        field = mutableListOf<ValidationError>()

    override fun addError(error: ValidationError) {
        errors += error
    }

    override fun raise(message: Message) = raise(ValidationError.Root(message))
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
