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

package proton.android.pass.data.impl.usecases.items

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.items.GetItemOptions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.items.ItemOption
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class GetItemOptionsImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val getVaultByShareId: GetVaultByShareId,
    private val encryptionContextProvider: EncryptionContextProvider
) : GetItemOptions {

    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        userId: UserId
    ): Option<List<ItemOption>> = combine(
        oneShot { getItemById(shareId = shareId, itemId = itemId) },
        oneShot { getVaultByShareId(userId, shareId).first() }
    ) { item, vault ->
        when (val itemType = item.itemType) {
            is ItemType.Login -> itemType.toItemOptions(vault)
            is ItemType.Alias,
            is ItemType.CreditCard,
            is ItemType.Identity,
            is ItemType.Note,
            is ItemType.Custom,
            is ItemType.WifiNetwork,
            is ItemType.SSHKey,
            ItemType.Password,
            ItemType.Unknown -> emptyList()
        }.let { itemOptions -> if (itemOptions.isEmpty()) None else Some(itemOptions) }
    }.catch { error ->
        PassLogger.w(TAG, "There was an error getting item options")
        PassLogger.w(TAG, error)
        emit(None)
    }.first()

    private fun ItemType.Login.toItemOptions(vault: Vault): List<ItemOption> = buildList {
        if (itemEmail.isNotBlank()) {
            ItemOption.CopyEmail(email = itemEmail).also(::add)
        }

        if (itemUsername.isNotBlank()) {
            ItemOption.CopyUsername(username = itemUsername).also(::add)
        }

        encryptionContextProvider.withEncryptionContext {
            decrypt(password)
        }.also { decryptedPassword ->
            if (decryptedPassword.isNotEmpty()) {
                ItemOption.CopyPassword(encryptedPassword = password).also(::add)
            }
        }

        if (vault.canBeUpdated) {
            ItemOption.MoveToTrash.also(::add)
        }
    }

    private companion object {

        private const val TAG = "GetItemOptionsImpl"

    }

}
