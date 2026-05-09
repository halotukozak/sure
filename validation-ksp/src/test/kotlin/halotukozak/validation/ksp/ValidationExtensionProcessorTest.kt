package halotukozak.validation.ksp

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class ValidationExtensionProcessorTest {
    @Test
    fun `generates validate extension for class with companion validator`() {
        val source =
            SourceFile.kotlin(
                "Demo.kt",
                """
                package demo

                import halotukozak.validation.Validatable
                import halotukozak.validation.Validator

                @Validatable
                data class Demo(val name: String) {
                    companion object {
                        val validator = Validator(Demo::class, true) {}
                    }
                }
                """.trimIndent(),
            )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = readGenerated(result)
        assertTrue(
            generated.contains("fun demo.Demo.validate(): halotukozak.validation.ValidationResult = demo.Demo.validator.validate(this)"),
            "missing validate() ext for Demo: $generated",
        )
        assertTrue(generated.contains("validatorsByClass"), "missing registry: $generated")
        assertTrue(generated.contains("inline fun <reified T : Any> validatorFor"), "missing validatorFor: $generated")
    }

    @Test
    fun `routes through with parameter when supplied`() {
        val source =
            SourceFile.kotlin(
                "Demo.kt",
                """
                package demo

                import halotukozak.validation.Validatable
                import halotukozak.validation.Validator

                @Validatable(with = DemoValidator::class)
                data class Demo(val name: String)

                object DemoValidator : Validator<Demo>(Demo::class, true, {})
                """.trimIndent(),
            )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = readGenerated(result)
        assertTrue(
            generated.contains("fun demo.Demo.validate(): halotukozak.validation.ValidationResult = demo.DemoValidator.validate(this)"),
            "expected DemoValidator route: $generated",
        )
    }

    @Test
    fun `errors when companion validator is missing`() {
        val source =
            SourceFile.kotlin(
                "Demo.kt",
                """
                package demo

                import halotukozak.validation.Validatable

                @Validatable
                data class Demo(val name: String)
                """.trimIndent(),
            )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertTrue(
            result.messages.contains("must declare a `validator` property"),
            "expected processor error in: ${result.messages}",
        )
    }

    private fun compile(vararg sources: SourceFile): JvmCompilationResult =
        KotlinCompilation()
            .apply {
                this.sources = sources.toList()
                inheritClassPath = true
                messageOutputStream = System.out
                configureKsp {
                    symbolProcessorProviders.add(ValidationExtensionProcessorProvider())
                }
            }.compile()

    private fun readGenerated(result: JvmCompilationResult): String {
        val kspDir = result.outputDirectory.parentFile.resolve("ksp/sources/kotlin")
        val file = File(kspDir, "halotukozak/validation/GeneratedValidationExtensions.kt")
        assertTrue(file.exists(), "expected generated file at $file")
        return file.readText()
    }
}
