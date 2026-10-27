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

package proton.android.pass.data.fakes.usecases.attachments

import proton.android.pass.data.api.usecases.attachments.DownloadAllAttachments
import proton.android.pass.data.api.usecases.attachments.DownloadAllResult
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDownloadAllAttachments @Inject constructor() : DownloadAllAttachments {

    private var result: DownloadAllResult = DownloadAllResult.Success
    private var exception: Throwable? = null

    var invocationCount = 0
        private set

    fun setResult(value: DownloadAllResult) {
        result = value
    }

    fun setException(value: Throwable) {
        exception = value
    }

    override suspend fun invoke(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): DownloadAllResult {
        invocationCount++
        exception?.let { throw it }
        return result
    }
}
