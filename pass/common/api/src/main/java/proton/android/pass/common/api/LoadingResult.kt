/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("TooManyFunctions")

package proton.android.pass.common.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.Logger

sealed interface LoadingResult<out T> {
    data class Success<T>(val data: T) : LoadingResult<T>
    data class Error(val exception: Throwable) : LoadingResult<Nothing>
    object Loading : LoadingResult<Nothing>
}

inline fun <R, T> LoadingResult<T>.map(transform: (value: T) -> R): LoadingResult<R> =
    when (this) {
        is LoadingResult.Success -> LoadingResult.Success(transform(data))
        is LoadingResult.Error -> LoadingResult.Error(exception)
        LoadingResult.Loading -> LoadingResult.Loading
    }

inline fun <T> LoadingResult<T>.onError(action: (exception: Throwable) -> Unit): LoadingResult<T> {
    if (this is LoadingResult.Error) {
        action(exception)
    }
    return this
}

inline fun <T> LoadingResult<T>.onSuccess(action: (value: T) -> Unit): LoadingResult<T> {
    if (this is LoadingResult.Success) {
        action(data)
    }
    return this
}

fun <T> LoadingResult<T>.logError(
    logger: Logger,
    tag: String,
    defaultMessage: String
): LoadingResult<T> {
    if (this is LoadingResult.Error) {
        logger.e(tag, exception, defaultMessage)
    }
    return this
}

inline fun <R, T> LoadingResult<T>.flatMap(transform: (value: T) -> LoadingResult<R>): LoadingResult<R> =
    when (this) {
        is LoadingResult.Success -> transform(data)
        is LoadingResult.Error -> LoadingResult.Error(exception)
        LoadingResult.Loading -> LoadingResult.Loading
    }


fun <T> LoadingResult<T>.getOrNull(): T? =
    when (this) {
        is LoadingResult.Error -> null
        is LoadingResult.Loading -> null
        is LoadingResult.Success -> this.data
    }

fun <T> Flow<T>.asLoadingResult(): Flow<LoadingResult<T>> =
    this
        .asResultWithoutLoading()
        .onStart { emit(LoadingResult.Loading) }


fun <T> Flow<T>.asResultWithoutLoading(): Flow<LoadingResult<T>> =
    this
        .map<T, LoadingResult<T>> {
            LoadingResult.Success(it)
        }
        .catch { emit(LoadingResult.Error(it)) }

fun <T> List<LoadingResult<T>>.transpose(): LoadingResult<List<T>> {
    val anyError = this.firstOrNull { it is LoadingResult.Error }
    if (anyError != null) {
        return anyError as LoadingResult.Error
    }

    val anyLoading = this.firstOrNull { it is LoadingResult.Loading }
    if (anyLoading != null) {
        return LoadingResult.Loading
    }

    val allValues = this.map { (it as LoadingResult.Success).data }
    return LoadingResult.Success(allValues)
}

@Suppress("TooGenericExceptionCaught")
fun <T> ApiResult<T>.toLoadingResult(): LoadingResult<T> =
    try {
        LoadingResult.Success(valueOrThrow)
    } catch (e: Throwable) {
        LoadingResult.Error(e)
    }

fun <T> T?.toLoadingResult(): LoadingResult<T> =
    this?.let { LoadingResult.Success(it) } ?: LoadingResult.Error(KotlinNullPointerException())

@Suppress("TooGenericExceptionCaught")
inline fun <T, R> T.runCatching(block: T.() -> R): LoadingResult<R> = try {
    LoadingResult.Success(block())
} catch (e: Throwable) {
    LoadingResult.Error(e)
}
