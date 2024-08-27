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
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemContents
import javax.inject.Inject

class SyncSimpleLoginPendingAliasesImpl @Inject constructor(
    private val repository: SimpleLoginRepository,
    private val createItem: CreateItem,
    private val shareKeyRepository: ShareKeyRepository
) : SyncSimpleLoginPendingAliases {

    override suspend fun invoke() {
        val syncStatus = repository.observeSyncStatus().first()

        if (!syncStatus.isSyncEnabled) {
            return
        }

        if (!syncStatus.hasPendingAliases) {
            return
        }

        var hasMorePendingAliases: Boolean
        val pendingAliasedDefaultShareId = syncStatus.defaultVault.shareId
        val shareKey = shareKeyRepository.getLatestKeyForShare(pendingAliasedDefaultShareId).first()

        do {
            repository.getPendingAliases()
                .let { pendingAliases ->
                    hasMorePendingAliases = pendingAliases.lastToken != null
                    pendingAliases.aliases
                }
                .map { alias ->
                    alias.id to createItem.create(
                        shareKey = shareKey,
                        itemContents = ItemContents.Alias(
                            title = alias.email,
                            note = alias.note,
                            aliasEmail = alias.email,
                            isDisabled = false
                        )
                    ).request
                }
                .also { pendingAliasesItems ->
                    repository.createPendingAliases(
                        defaultShareId = pendingAliasedDefaultShareId,
                        pendingAliasesItems = pendingAliasesItems
                    )
                }
        } while (hasMorePendingAliases)
    }

}
