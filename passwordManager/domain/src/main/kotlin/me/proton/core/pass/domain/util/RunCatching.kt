package me.proton.core.pass.domain.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

inline fun <R, reified T : Throwable> Result<R>.except(): Result<R> =
    onFailure { if (it is T) throw it }

inline fun <T> coRunCatching(block: () -> T) = runCatching(block).except<T, CancellationException>()

suspend inline fun <T> coRunCatching(
    coroutineContext: CoroutineContext,
    crossinline block: suspend () -> T
) =
    withContext(coroutineContext) { coRunCatching { block() } }
