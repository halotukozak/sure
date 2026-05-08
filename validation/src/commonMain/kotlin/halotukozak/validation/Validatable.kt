package halotukozak.validation

import kotlin.reflect.KClass

/**
 * Marks a class as participating in the validation framework.
 *
 * Annotated class must declare a companion object with a `validator: Validator<T>` property,
 * or pass `with = SomeValidatorObject::class`. The KSP processor in `:validation-ksp` scans every
 * `@Validatable` class at compile time and emits, into the consuming module, a generated file
 * containing:
 *
 *  * `fun T.validate(): ValidationResult` — calls validator on the receiver.
 *  * `validatorsByClass: Map<KClass<*>, Validator<*>>` registry, plus inline `validatorFor<T>()` lookup.
 */
@Target(AnnotationTarget.CLASS)
annotation class Validatable(
    val with: KClass<out Validator<*>> = Validator::class,
)
