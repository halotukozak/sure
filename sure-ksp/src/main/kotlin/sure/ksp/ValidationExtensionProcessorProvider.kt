package sure.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.io.OutputStreamWriter

private const val ANNOTATION_FQN = "sure.Validatable"
private const val VALIDATOR_FQN = "sure.Validator"
private const val VALIDATION_RESULT_FQN = "sure.ValidationResult"
private const val GENERATED_PACKAGE = "sure"
private const val GENERATED_FILE = "GeneratedValidationExtensions"
private const val VALIDATOR_FIELD = "validator"
private const val WITH_ARG = "with"

class ValidationExtensionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ValidationExtensionProcessor(environment.codeGenerator, environment.logger)
}

private class ValidationExtensionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val classes =
            resolver
                .getSymbolsWithAnnotation(ANNOTATION_FQN, false)
                .filterIsInstance<KSClassDeclaration>()
                .toList()
        if (classes.isEmpty()) return emptyList()

        val refs = classes.mapNotNull { it.toValidatorRef() }
        if (refs.isEmpty()) return emptyList()

        val originatingFiles = classes.mapNotNull { it.containingFile }.distinct()
        codeGenerator
            .createNewFile(
                Dependencies(false, *originatingFiles.toTypedArray()),
                GENERATED_PACKAGE,
                GENERATED_FILE,
            ).use { stream -> OutputStreamWriter(stream).use { it.write(render(refs)) } }

        generated = true
        return emptyList()
    }

    private fun KSClassDeclaration.toValidatorRef(): ValidatorRef? {
        val receiverFqn =
            qualifiedName?.asString() ?: run {
                logger.warn("@Validatable type ${simpleName.asString()} has no qualified name", this)
                return null
            }
        val validatorExpression = customValidatorFqn() ?: companionValidatorExpression(receiverFqn) ?: return null
        return ValidatorRef(receiverFqn, validatorExpression)
    }

    private fun KSClassDeclaration.companionValidatorExpression(receiverFqn: String): String? {
        val companion = declarations.filterIsInstance<KSClassDeclaration>().firstOrNull { it.isCompanionObject }
        val hasValidator = companion?.getAllProperties()?.any { it.simpleName.asString() == VALIDATOR_FIELD } == true
        return if (!hasValidator) {
            logger.error(
                "@Validatable class ${simpleName.asString()} must declare a `$VALIDATOR_FIELD` property in its " +
                    "companion object, or specify @Validatable($WITH_ARG = SomeObject::class)",
                this,
            )
            null
        } else {
            "$receiverFqn.$VALIDATOR_FIELD"
        }
    }

    private fun KSClassDeclaration.customValidatorFqn(): String? {
        val annotation =
            annotations.firstOrNull {
                it.annotationType
                    .resolve()
                    .declaration.qualifiedName
                    ?.asString() == ANNOTATION_FQN
            } ?: return null

        val withType =
            annotation.arguments
                .firstOrNull { it.name?.asString() == WITH_ARG }
                ?.value as? KSType ?: return null

        val withFqn = withType.declaration.qualifiedName?.asString()
        return withFqn?.takeUnless { it == VALIDATOR_FQN }
    }

    private fun render(refs: List<ValidatorRef>): String =
        buildString {
            appendLine("package $GENERATED_PACKAGE")
            appendLine()
            for (ref in refs) {
                appendLine(
                    "fun ${ref.receiverFqn}.validate(): $VALIDATION_RESULT_FQN = " +
                        "${ref.validatorExpression}.validate(this)",
                )
                appendLine()
            }
            appendLine("@PublishedApi")
            appendLine("internal val validatorsByClass: Map<kotlin.reflect.KClass<*>, $VALIDATOR_FQN<*>> = mapOf(")
            for (ref in refs) {
                appendLine("    ${ref.receiverFqn}::class to ${ref.validatorExpression},")
            }
            appendLine(")")
            appendLine()
            appendLine("@Suppress(\"UNCHECKED_CAST\")")
            appendLine("inline fun <reified T : Any> validatorFor(): $VALIDATOR_FQN<T> =")
            appendLine("    validatorsByClass[T::class] as? $VALIDATOR_FQN<T>")
            appendLine($$"        ?: error(\"No validator registered for ${T::class.qualifiedName}\")")
        }
}

private data class ValidatorRef(
    val receiverFqn: String,
    val validatorExpression: String,
)
