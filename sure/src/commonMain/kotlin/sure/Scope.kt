package sure

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@DslMarker
annotation class ValidationDsl

@ValidationDsl
sealed class ValidationScope<out T> {
    internal abstract val path: String

    @PublishedApi
    internal abstract val shortCircuit: Boolean

    internal abstract fun addError(error: ValidationError)

    abstract fun raise(message: Message)

    /** Reports [error] and, when fail-fast, unwinds to the nearest runScope { } so siblings stop running. */
    protected fun raise(error: ValidationError) {
        addError(error)
        if (shortCircuit) throw ScopeShortCircuit()
    }
}

@PublishedApi
internal sealed class ParentScope<out T> : ValidationScope<T>() {
    val errors: List<ValidationError>
        field = mutableListOf<ValidationError>()

    final override fun addError(error: ValidationError) {
        errors += error
    }
}

@PublishedApi
internal class RootScope<out T>(
    val value: T,
    override val shortCircuit: Boolean,
) : ParentScope<T>() {
    override val path: String = ""

    override fun raise(message: Message) = raise(ValidationError.Root(message))
}

@PublishedApi
internal sealed class ChildrenScope<out T> : ValidationScope<T>() {
    abstract val parent: ValidationScope<*>

    final override fun addError(error: ValidationError) {
        parent.addError(error)
    }
}

@PublishedApi
internal class FieldScope<out T>(
    val value: T,
    name: String,
    override val parent: ValidationScope<*>,
    override val shortCircuit: Boolean = parent.shortCircuit,
) : ChildrenScope<T>() {
    override val path: String = if (parent.path.isEmpty()) name else "${parent.path}.$name"

    override fun raise(message: Message) = raise(ValidationError.Field(path, message))
}

@PublishedApi
internal class ItemScope<out T>(
    val value: T,
    private val index: Int,
    override val parent: ValidationScope<*>,
    override val shortCircuit: Boolean = parent.shortCircuit,
) : ChildrenScope<T>() {
    override val path = "${parent.path}[$index]"

    override fun raise(message: Message) = raise(ValidationError.Element(parent.path, index, message))
}

@PublishedApi
internal class EntryScope<out T>(
    val value: T,
    key: Any?,
    override val parent: ValidationScope<*>,
    override val shortCircuit: Boolean = parent.shortCircuit,
) : ChildrenScope<T>() {
    override val path: String = "${parent.path}[$key]"

    override fun raise(message: Message) = raise(ValidationError.Field(path, message))
}

@PublishedApi
internal class EphemeralScope<out T>(
    val value: T,
    parent: ValidationScope<*>,
    override val shortCircuit: Boolean = parent.shortCircuit,
) : ParentScope<T>() {
    override val path: String = parent.path

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
