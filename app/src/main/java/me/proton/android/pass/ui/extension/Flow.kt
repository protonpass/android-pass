package me.proton.android.pass.ui.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.onSuccess

fun <T, R> Flow<T>.toStateFlow(scope: CoroutineScope, transform: (T) -> R?): StateFlow<R?> =
    MutableStateFlow<R?>(null).apply {
        this@toStateFlow.onEach { value ->
            transform(value)?.let { transformed ->
                emit(transformed)
            }
        }.launchIn(scope)
    }

fun <T> Flow<DataResult<T>>.mapSuccessToStateFlow(scope: CoroutineScope): StateFlow<T?> =
    toStateFlow(scope) { result ->
        result.onSuccess { value ->
            return@toStateFlow value
        }
        null
    }
