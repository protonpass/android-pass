package me.proton.pass.common.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.Logger

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
    object Loading : Result<Nothing>
}

inline fun <R, T> Result<T>.map(transform: (value: T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        Result.Loading -> Result.Loading
    }

inline fun <T> Result<T>.onError(action: (exception: Throwable?) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

fun <T> Result<T>.logError(logger: Logger, tag: String, defaultMessage: String): Result<T> {
    if (this is Result.Error) {
        logger.e(tag, exception ?: RuntimeException(defaultMessage), defaultMessage)
    }
    return this
}

inline fun <R, T> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> Result.Error(exception)
        Result.Loading -> Result.Loading
    }

fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .asResultWithoutLoading()
        .onStart { emit(Result.Loading) }


fun <T> Flow<T>.asResultWithoutLoading(): Flow<Result<T>> =
    this
        .map<T, Result<T>> {
            Result.Success(it)
        }
        .catch { emit(Result.Error(it)) }

fun <T> List<Result<T>>.transpose(): Result<List<T>> {
    val anyError = this.firstOrNull { it is Result.Error }
    if (anyError != null) {
        return anyError as Result.Error
    }

    val anyLoading = this.firstOrNull { it is Result.Loading }
    if (anyLoading != null) {
        return Result.Loading
    }

    val allValues = this.map { (it as Result.Success).data }
    return Result.Success(allValues)
}

@Suppress("TooGenericExceptionCaught")
fun <T> ApiResult<T>.toResult(): Result<T> =
    try {
        Result.Success(valueOrThrow)
    } catch (e: Throwable) {
        Result.Error(e)
    }

fun <T> T?.toResult(): Result<T> = this?.let { Result.Success(it) } ?: Result.Error()
