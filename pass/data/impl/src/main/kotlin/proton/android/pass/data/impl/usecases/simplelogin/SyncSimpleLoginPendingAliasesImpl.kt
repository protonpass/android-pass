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

package proton.android.pass.data.impl.usecases.simplelogin

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class SyncSimpleLoginPendingAliasesImpl @Inject constructor(
    private val repository: SimpleLoginRepository,
    private val createItem: CreateItem,
    private val shareKeyRepository: ShareKeyRepository,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val observeVaults: ObserveVaults
) : SyncSimpleLoginPendingAliases {

    @Suppress("ReturnCount")
    override suspend fun invoke(userId: UserId, forceRefresh: Boolean) {
        val userAccessData = userAccessDataRepository.observe(userId).first()
        if (userAccessData?.isSimpleLoginSyncEnabled != true) {
            return
        }

        val syncStatus = try {
            repository.observeSyncStatus(userId, forceRefresh).first()
        } catch (e: ShareNotAvailableError) {
            PassLogger.w(TAG, "SL default share not available during sync")
            PassLogger.w(TAG, e)
            recoverAndGetSyncStatus(userId) ?: return
        } catch (e: ShareContentNotAvailableError) {
            PassLogger.w(TAG, "SL default share content unavailable during sync")
            PassLogger.w(TAG, e)
            recoverAndGetSyncStatus(userId) ?: return
        }

        if (!syncStatus.isSyncEnabled) {
            return
        }

        var hasMorePendingAliases: Boolean
        val pendingAliasedDefaultShareId = syncStatus.defaultVault.shareId
        val shareKey = shareKeyRepository.getLatestKeyForShare(pendingAliasedDefaultShareId).first()

        do {
            val pendingAliases = repository.getPendingAliases(userId)
            hasMorePendingAliases = pendingAliases.lastToken != null
            val requests = pendingAliases.aliases.map { alias ->
                alias.id to requestForItem(shareKey, alias.email)
            }

            repository.createPendingAliases(
                userId = userId,
                defaultShareId = pendingAliasedDefaultShareId,
                pendingAliasesItems = requests
            )
        } while (hasMorePendingAliases)
    }

    private fun requestForItem(shareKey: ShareKey, email: String): EncryptedCreateItem = createItem.create(
        parentKey = shareKey,
        itemContents = ItemContents.Alias(
            title = email,
            aliasEmail = email,
            note = "",
            customFields = emptyList()
        )
    )

    private suspend fun getOwnedFallbackVault(userId: UserId): Vault? = observeVaults(
        userId = userId,
        includeHidden = true
    ).first().let { vaults ->
        vaults.firstOrNull { vault -> vault.isOwned && !vault.shareFlags.isHidden() }
            ?: vaults.firstOrNull { vault -> vault.isOwned }
    }

    private suspend fun recoverAndGetSyncStatus(userId: UserId): SimpleLoginSyncStatus? {
        val fallbackVault = getOwnedFallbackVault(userId) ?: run {
            PassLogger.w(TAG, "Missing SL default share and no owned fallback vault found")
            return null
        }

        PassLogger.w(TAG, "Missing SL default share. Recovering with owned vault [${fallbackVault.shareId.id}]")
        runCatching {
            repository.enableSync(fallbackVault.shareId)
        }.getOrElse { e ->
            PassLogger.w(TAG, "SL sync enable failed during recovery attempt")
            PassLogger.w(TAG, e)
            return null
        }

        return runCatching {
            repository.observeSyncStatus(userId, forceRefresh = true).first()
        }.getOrElse { e ->
            PassLogger.w(TAG, "SL sync status fetch failed after recovery attempt")
            PassLogger.w(TAG, e)
            null
        }
    }

    private companion object {
        private const val TAG = "SyncSimpleLoginPendingAliasesImpl"
    }
}
