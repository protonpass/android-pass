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

package proton.android.pass.data.impl.usecases.passkeys

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveItemsWithPasskeysImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val localItemDataSource: LocalItemDataSource,
    private val shareRepository: ShareRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveItemsWithPasskeys {
    override fun invoke(
        userId: UserId?,
        shareSelection: ShareSelection,
        includeHiddenVault: Boolean
    ): Flow<List<Item>> = (userId?.let(::flowOf) ?: accountManager.getPrimaryUserId())
        .filterNotNull()
        .flatMapLatest { resolvedUserId ->
            when (shareSelection) {
                is ShareSelection.Share -> localItemDataSource.observeItemsWithPasskeys(
                    userId = resolvedUserId,
                    shareIds = listOf(shareSelection.shareId)
                )

                is ShareSelection.Shares -> localItemDataSource.observeItemsWithPasskeys(
                    userId = resolvedUserId,
                    shareIds = shareSelection.shareIds
                )

                ShareSelection.AllShares ->
                    shareRepository.observeAllUsableShareIds(resolvedUserId, includeHiddenVault)
                        .flatMapLatest { list ->
                            localItemDataSource.observeItemsWithPasskeys(resolvedUserId, list)
                        }
            }
        }
        .mapLatest { items ->
            encryptionContextProvider.withEncryptionContext {
                items.map { it.toDomain(this@withEncryptionContext) }
            }
        }
}
