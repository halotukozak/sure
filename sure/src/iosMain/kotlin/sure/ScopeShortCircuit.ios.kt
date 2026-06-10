package sure

import kotlin.coroutines.cancellation.CancellationException

@PublishedApi
internal actual class ScopeShortCircuit actual constructor() : CancellationException("validation short-circuit")
