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
import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.assetlink.AssetLink
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
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
        val collected = ConcurrentLinkedQueue<AssetLink>()
        val failureCount = AtomicInteger(0)
        val sizeErrorCount = AtomicInteger(0)

        val completed = withTimeoutOrNull(FETCH_TIMEOUT_MS) {
            runConcurrently(
                items = cleanWebsites,
                block = assetLinkRepository::fetch,
                onSuccess = { _, assetLink -> collected.add(assetLink) },
                onFailure = { _, e ->
                    failureCount.incrementAndGet()
                    if (e is ResponseSizeExceededError) sizeErrorCount.incrementAndGet()
                }
            )
        }

        if (completed == null) {
            PassLogger.w(TAG, "Timed out fetching asset links, saving ${collected.size} partial results")
        }
        if (sizeErrorCount.get() > 0) {
            PassLogger.w(TAG, "${sizeErrorCount.get()} websites returned oversized responses")
        }
        if (failureCount.get() > 0) {
            PassLogger.w(TAG, "${failureCount.get()} from ${cleanWebsites.size} websites failed to get asset links")
        }
        val assetLinks = collected.toList()
        if (assetLinks.isNotEmpty()) {
            assetLinkRepository.insert(assetLinks)
        }
    }

    companion object {
        private const val TAG = "UpdateAssetLinkWorkerImpl"
        private const val FFI_TIMEOUT_MS = 60 * 1000L
        private const val FETCH_TIMEOUT_MS = 8 * 60 * 1000L
    }
}
