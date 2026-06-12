package sure.ksp

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

                import sure.Validatable
                import sure.Validator

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
            generated.contains("fun demo.Demo.validate(): sure.ValidationResult = demo.Demo.validator.validate(this)"),
            "missing validate() ext for Demo: $generated",
        )
        assertTrue(generated.contains("validatorsByClass"), "missing registry: $generated")
        assertTrue(generated.contains("inline fun <reified T : Any> validatorFor"), "missing validatorFor: $generated")
        assertTrue(
            generated.contains("inline fun <reified F : Any> validated(property: kotlin.reflect.KProperty0<F>)"),
            "missing validated(property) overload: $generated",
        )
    }

    @Test
    fun `routes through with parameter when supplied`() {
        val source =
            SourceFile.kotlin(
                "Demo.kt",
                """
                package demo

                import sure.Validatable
                import sure.Validator

                @Validatable(with = DemoValidator::class)
                data class Demo(val name: String)

                object DemoValidator : Validator<Demo>(Demo::class, true, {})
                """.trimIndent(),
            )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = readGenerated(result)
        assertTrue(
            generated.contains("fun demo.Demo.validate(): sure.ValidationResult = demo.DemoValidator.validate(this)"),
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

                import sure.Validatable

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
        val file = File(kspDir, "sure/GeneratedValidationExtensions.kt")
        assertTrue(file.exists(), "expected generated file at $file")
        return file.readText()
    }
}
