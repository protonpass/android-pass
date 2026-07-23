/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.impl.usecases.assetlink

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class UpdateAssetLinkImpl @Inject constructor(
    private val assetLinkRepository: AssetLinkRepository,
    private val domainManager: DomainManager
) : UpdateAssetLink {
    override suspend fun invoke(websites: Set<String>) {
        val cleanWebsites = withContext(Dispatchers.IO) {
            withTimeoutOrNull(FFI_TIMEOUT_MS) {
                websites.filter(String::isNotBlank)
                    .mapNotNull { runInterruptible { domainManager.getRoot(it) } }
                    .map { "https://$it" }
                    .toSet()
            } ?: run {
                PassLogger.w(TAG, "Timed out resolving root domains from ${websites.size} websites")
                emptySet()
            }
        }
        var failureCount = 0
        var sizeErrorCount = 0
        var persistedAssetLinkCount = 0

        val completed = withTimeoutOrNull(FETCH_TIMEOUT_MS) {
            cleanWebsites.forEach { website ->
                val assetLinkResult = safeRunCatching { assetLinkRepository.fetch(website) }
                val assetLink = assetLinkResult.getOrNull()
                if (assetLink == null) {
                    failureCount += 1
                    if (assetLinkResult.exceptionOrNull() is ResponseSizeExceededError) {
                        sizeErrorCount += 1
                    }
                } else {
                    assetLinkRepository.insert(listOf(assetLink))
                    persistedAssetLinkCount += 1
                }
            }
        }

        if (completed == null) {
            PassLogger.w(TAG, "Timed out fetching asset links after saving $persistedAssetLinkCount results")
        }
        if (sizeErrorCount > 0) {
            PassLogger.w(TAG, "$sizeErrorCount websites returned oversized responses")
        }
        if (failureCount > 0) {
            PassLogger.w(TAG, "$failureCount from ${cleanWebsites.size} websites failed to get asset links")
        }
    }

    companion object {
        private const val TAG = "UpdateAssetLinkWorkerImpl"
        private const val FFI_TIMEOUT_MS = 60 * 1000L
        private const val FETCH_TIMEOUT_MS = 8 * 60 * 1000L
    }
}
