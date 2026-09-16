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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.GetPrimaryUserId
import proton.android.pass.data.api.usecases.ImageResponseResult
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import javax.inject.Inject

class RequestImageImpl @Inject constructor(
    private val fetcher: RemoteImageFetcher,
    private val getPrimaryUserId: GetPrimaryUserId
) : RequestImage {
    override fun invoke(domain: String): Flow<ImageResponseResult> = flow {
        val parsed = UrlSanitizer.getDomain(domain).getOrThrow()
        val userId = getPrimaryUserId() ?: throw UserIdNotAvailableError()
        fetcher.fetchFavicon(userId, parsed)
            .catch { emit(ImageResponseResult.Error(it)) }
            .collect {
                if (it == null) {
                    emit(ImageResponseResult.Empty)
                } else {
                    emit(ImageResponseResult.Data(it.content, it.mimeType))
                }
            }
    }
}
