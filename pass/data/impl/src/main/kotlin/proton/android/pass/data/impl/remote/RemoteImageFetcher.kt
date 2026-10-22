/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import proton.android.pass.data.api.usecases.ImageResponse
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteApiGateway
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.network.get
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface RemoteImageFetcher {
    fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?>
}

class RemoteImageFetcherImpl @Inject constructor(
    private val api: RemoteApiGateway
) : RemoteImageFetcher {

    override fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?> = flow {
        for (size in SIZES) {
            val result = api.get<PasswordManagerApi>(userId).invoke {
                val res = getFavicon(domain = domain, size = size)
                if (res.code() == HTTP_NO_CONTENT) {
                    null
                } else {
                    val body = checkNotNull(res.body()?.bytes())
                    ImageResponse(content = body, mimeType = res.headers()["Content-Type"])
                }
            }
            when (result) {
                is RemoteResult.Success -> {
                    emit(result.value)
                    return@flow
                }
                is RemoteResult.Error -> when (handleError(result, domain, size)) {
                    ExceptionHandleResult.EmitNull -> {
                        emit(null)
                        return@flow
                    }
                    ExceptionHandleResult.Continue -> Unit
                }
            }
        }

        emit(null)
    }

    private fun handleError(
        error: RemoteResult.Error,
        domain: String,
        size: Int
    ): ExceptionHandleResult {
        if (error !is RemoteResult.Error.Http || error.status != HTTP_UNPROCESSABLE_CONTENT) {
            throw RemoteApiException(error)
        }

        val knownError = FetchImageError.entries.firstOrNull { it.code == error.protonCode }

        return when (knownError) {
            FetchImageError.FailedToFindForAppropriateSize -> {
                PassLogger.d(TAG, "Received ${knownError.name} for domain $domain with size $size")
                ExceptionHandleResult.Continue
            }
            null -> {
                PassLogger.d(TAG, "Received unknown ProtonErrorCode: ${error.protonCode}")
                throw RemoteApiException(error)
            }
            else -> {
                PassLogger.d(TAG, "Received ${knownError.name} for domain $domain")
                ExceptionHandleResult.EmitNull
            }
        }
    }

    private sealed interface ExceptionHandleResult {
        data object Continue : ExceptionHandleResult
        data object EmitNull : ExceptionHandleResult
    }

    private enum class FetchImageError(val code: Int) {
        NotTrusted(2011),
        InvalidAddress(-1),
        FailedToFindForAppropriateSize(2511),
        FailedToFind(2902)
    }

    companion object {
        private const val HTTP_UNPROCESSABLE_CONTENT = 422
        private const val HTTP_NO_CONTENT = 204
        private val SIZES = listOf(64, 32)

        private const val TAG = "RemoteImageFetcherImpl"
    }
}
