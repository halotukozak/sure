package sure

sealed interface ValidationError {
    val message: Message

    data class Field(
        val path: String,
        override val message: Message,
    ) : ValidationError {
        override fun toString(): String = "$path: $message"
    }

    data class Element(
        val path: String,
        val index: Int,
        override val message: Message,
    ) : ValidationError {
        override fun toString(): String = "$path[$index]: $message"
    }

    data class Root(
        override val message: Message,
    ) : ValidationError {
        override fun toString(): String = message.toString()
    }
}
