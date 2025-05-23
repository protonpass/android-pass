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

package proton.android.pass.data.impl.usecases.simplelogin

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class SyncSimpleLoginPendingAliasesImpl @Inject constructor(
    private val repository: SimpleLoginRepository,
    private val createItem: CreateItem,
    private val shareKeyRepository: ShareKeyRepository
) : SyncSimpleLoginPendingAliases {

    override suspend fun invoke(userId: UserId) {
        val syncStatus = repository.observeSyncStatus(userId).first()

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
        shareKey = shareKey,
        itemContents = ItemContents.Alias(
            title = email,
            aliasEmail = email,
            note = "",
            customFields = emptyList()
        )
    ).request
}
