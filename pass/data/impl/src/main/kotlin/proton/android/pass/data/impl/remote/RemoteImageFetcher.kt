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

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.ProtonErrorException
import proton.android.pass.data.api.usecases.ImageResponse
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface RemoteImageFetcher {
    fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?>
}

class RemoteImageFetcherImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteImageFetcher {

    @Suppress("MagicNumber")
    override fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?> = flow {
        api.get<PasswordManagerApi>(userId).invoke {
            for (size in listOf(64, 32)) {
                try {
                    val res = getFavicon(domain = domain, size = size)
                    if (res.code() == HTTP_NO_CONTENT) {
                        emit(null)
                        return@invoke
                    }

                    val body = checkNotNull(res.body()?.bytes())
                    val mimeType = res.headers()["Content-Type"]
                    emit(ImageResponse(content = body, mimeType = mimeType))
                    return@invoke
                } catch (e: ProtonErrorException) {
                    when (handleError(e, domain, size)) {
                        ExceptionHandleResult.EmitNull -> {
                            emit(null)
                            return@invoke
                        }
                        ExceptionHandleResult.Continue -> {
                            continue
                        }
                    }
                }
            }

            // Could not find any image
            emit(null)
        }
    }

    private fun handleError(
        e: ProtonErrorException,
        domain: String,
        size: Int
    ): ExceptionHandleResult {
        if (e.response.code != HTTP_UNPROCESSABLE_CONTENT) throw e

        val knownError = FetchImageError.values()
            .firstOrNull { it.code == e.protonData.code }

        when (knownError) {
            FetchImageError.FailedToFindForAppropriateSize -> {
                PassLogger.d(TAG, "Received ${knownError.name} for domain $domain with size $size")
                return ExceptionHandleResult.Continue
            }
            null -> {
                PassLogger.d(TAG, "Received unknown ProtonErrorCode: ${e.protonData.code}")
                throw e
            }
            else -> {
                PassLogger.d(TAG, "Received ${knownError.name} for domain $domain")
                return ExceptionHandleResult.EmitNull
            }
        }
    }

    private sealed interface ExceptionHandleResult {
        object Continue : ExceptionHandleResult
        object EmitNull : ExceptionHandleResult
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

        private const val TAG = "RemoteImageFetcherImpl"
    }
}
