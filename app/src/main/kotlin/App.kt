package halotukozak.app

import halotukozak.validation.Validatable
import halotukozak.validation.Validator
import halotukozak.validation.field
import halotukozak.validation.lengthIn
import halotukozak.validation.notBlank
import halotukozak.validation.validate

@Validatable
data class LoginRequest(val username: String, val password: String) {
    companion object {
        val validator = Validator<LoginRequest> {
            field(::username) { notBlank(); lengthIn(1..254) }
            field(::password) { notBlank(); lengthIn(8..1024) }
        }
    }
}

fun main() {
    println(LoginRequest("alice", "hunter22!").validate())
    println(LoginRequest("", "x").validate())
}
