package halotukozak.validation

import kotlin.coroutines.cancellation.CancellationException

@PublishedApi
internal actual class ScopeShortCircuit actual constructor() : CancellationException("validation short-circuit") {
    // Pure control flow — never logged or inspected. Skip the stack-trace fill cost.
    override fun fillInStackTrace(): Throwable = this
}
