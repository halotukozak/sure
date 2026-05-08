package halotukozak.validation

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

@OptIn(ExperimentalContracts::class)
private fun <T : Any> Any.isInstanceOf(kClass: KClass<T>): Boolean {
    contract {
        returns(true) implies (this@isInstanceOf is T)
    }
    return kClass.isInstance(this)
}

open class Validator<T>(
    protected val kClass: KClass<T & Any>,
    internal val shortCircuit: Boolean,
    internal val applyRules: ValidationScope<T>.() -> Unit,
) {
    open fun validate(value: Any?): ValidationResult = when {
        value == null -> {
            ValidationResult.Invalid(ValidationError.Root(ValidationError.Message.NotNull))
        }

        !value.isInstanceOf(kClass) -> {
            ValidationResult.Invalid(
                ValidationError.Root(ValidationError.Message.TypeMismatched(kClass, value::class)),
            )
        }

        else -> {
            val scope = RootScope(value, shortCircuit)
            runScope { applyRules(scope) }
            if (scope.errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(scope.errors)
        }
    }

    fun nullable(): Validator<T?> =
        object : Validator<T?>(kClass, shortCircuit, { if (value != null) applyRules(this) }) {
            override fun validate(value: Any?): ValidationResult =
                if (value == null) ValidationResult.Valid else this@Validator.validate(value)
        }

    fun failFast(): Validator<T> = Validator(kClass, true, applyRules)

    fun accumulating(): Validator<T> = Validator(kClass, false, applyRules)

    companion object {
        private val reusableEmpty = Validator(Any::class, true) {}

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): Validator<T> = reusableEmpty as Validator<T>

        inline operator fun <reified T : Any> invoke(
            shortCircuit: Boolean = true,
            noinline rules: context(ValidationScope<T>)(T).() -> Unit,
        ): Validator<T> = Validator(T::class, shortCircuit) { rules(value) }
    }
}
