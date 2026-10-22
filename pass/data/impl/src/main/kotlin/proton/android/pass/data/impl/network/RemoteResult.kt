/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.network

import proton.android.pass.data.api.errors.RetryableException

sealed interface RemoteResult<out T> {

    data class Success<T>(val value: T) : RemoteResult<T>

    sealed interface Error : RemoteResult<Nothing> {

        val cause: Throwable?

        val isRetryable: Boolean

        data class Http(
            val status: Int,
            val protonCode: Int?,
            val message: String,
            override val cause: Throwable? = null,
            override val isRetryable: Boolean = false
        ) : Error

        data class NoInternet(
            override val cause: Throwable? = null,
            override val isRetryable: Boolean = true
        ) : Error

        data class Failure(
            override val cause: Throwable? = null,
            override val isRetryable: Boolean = false
        ) : Error
    }
}

class RemoteApiException(
    val error: RemoteResult.Error
) : Exception(
    (error as? RemoteResult.Error.Http)?.message ?: error.cause?.message,
    error.cause
),
    RetryableException {

    override val isRetryable: Boolean
        get() = error.isRetryable
}

val <T> RemoteResult<T>.valueOrThrow: T
    get() = when (this) {
        is RemoteResult.Success -> value
        is RemoteResult.Error -> throw RemoteApiException(this)
    }

fun RemoteResult<*>.throwIfError() {
    if (this is RemoteResult.Error) throw RemoteApiException(this)
}
