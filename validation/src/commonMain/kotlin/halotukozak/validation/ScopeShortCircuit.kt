package halotukozak.validation

import kotlin.coroutines.cancellation.CancellationException

@PublishedApi
internal expect class ScopeShortCircuit() : CancellationException

@PublishedApi
internal inline fun runScope(block: () -> Unit) = try {
    block()
} catch (_: ScopeShortCircuit) {
    // expected: short-circuit unwinds here so siblings still run
}
