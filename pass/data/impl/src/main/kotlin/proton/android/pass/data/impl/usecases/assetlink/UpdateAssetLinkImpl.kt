/*
 * Copyright (c) 2024 Proton AG
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

import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.assetlink.AssetLink
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class UpdateAssetLinkImpl @Inject constructor(
    private val assetLinkRepository: AssetLinkRepository,
    private val domainManager: DomainManager
) : UpdateAssetLink {
    override suspend fun invoke(websites: Set<String>) {
        val cleanWebsites = websites.filter(String::isNotBlank)
            .mapNotNull(domainManager::getRoot)
            .map { "https://$it" }
            .toSet()
        val results: List<Result<AssetLink>> = runConcurrently(
            items = cleanWebsites,
            block = assetLinkRepository::fetch
        )
        val (successes, failures) = results.partition { it.isSuccess }
        if (failures.isNotEmpty()) {
            PassLogger.w(
                TAG,
                "${failures.size} from ${results.size} websites failed to get asset links"
            )
        }
        val assetLinks = successes.mapNotNull { it.getOrNull() }
        if (assetLinks.isNotEmpty()) {
            assetLinkRepository.insert(assetLinks)
        }
    }

    companion object {
        private const val TAG = "UpdateAssetLinkWorkerImpl"
    }
}
