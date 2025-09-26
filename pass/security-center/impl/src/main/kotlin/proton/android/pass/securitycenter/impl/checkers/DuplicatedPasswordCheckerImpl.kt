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

package proton.android.pass.securitycenter.impl.checkers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareFlag
import proton.android.pass.domain.ShareSelection
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordReport
import javax.inject.Inject

class DuplicatedPasswordCheckerImpl @Inject constructor(
    private val observeItems: ObserveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : DuplicatedPasswordChecker {

    override suspend fun invoke(item: Item): DuplicatedPasswordReport {
        if (item.itemType !is ItemType.Login) {
            return DuplicatedPasswordReport(emptySet())
        }

        val itemPassword = encryptionContextProvider.withEncryptionContext {
            decrypt((item.itemType as ItemType.Login).password)
        }

        if (itemPassword.isBlank()) {
            return DuplicatedPasswordReport(emptySet())
        }

        val items = observeItems(
            selection = ShareSelection.AllShares,
            itemState = ItemState.Active,
            filter = ItemTypeFilter.Logins,
            itemFlags = mapOf(ItemFlag.SkipHealthCheck to false),
            shareFlags = mapOf(ShareFlag.IsHidden to false)
        ).first().filter { loginItem -> loginItem.id != item.id }

        return withContext(Dispatchers.Default) {
            val duplicatedPasswordItems = mutableSetOf<Item>()
            encryptionContextProvider.withEncryptionContext {
                items.forEach { loginItem ->
                    if (loginItem.itemType is ItemType.Login) {
                        val currentItemPassword =
                            decrypt((loginItem.itemType as ItemType.Login).password)
                        if (itemPassword == currentItemPassword) {
                            duplicatedPasswordItems.add(loginItem)
                        }
                    }
                }
            }
            DuplicatedPasswordReport(duplicatedPasswordItems)
        }
    }

}
