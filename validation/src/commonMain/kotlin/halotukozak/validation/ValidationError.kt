package halotukozak.validation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ValidationError {
    val message: Message

    @Serializable
    @SerialName("field")
    data class Field(val path: String, override val message: Message) : ValidationError {
        override fun toString(): String = "$path: $message"
    }

    @Serializable
    @SerialName("element")
    data class Element(
        val path: String,
        val index: Int,
        override val message: Message,
    ) : ValidationError {
        override fun toString(): String = "$path[$index]: $message"
    }

    @Serializable
    @SerialName("root")
    data class Root(override val message: Message) : ValidationError {
        override fun toString(): String = message.toString()
    }
}
