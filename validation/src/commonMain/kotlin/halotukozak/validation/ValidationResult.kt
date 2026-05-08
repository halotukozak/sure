package halotukozak.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(val errors: List<ValidationError>) : ValidationResult {
        constructor(head: ValidationError, vararg t: ValidationError) : this(listOf(head, *t))
    }
}
