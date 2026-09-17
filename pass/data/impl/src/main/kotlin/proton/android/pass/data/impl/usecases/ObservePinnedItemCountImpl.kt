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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObservePinnedItemCount
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.items.ItemCategory
import javax.inject.Inject

class ObservePinnedItemCountImpl @Inject constructor(
    private val observePinnedItems: ObservePinnedItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObservePinnedItemCount {

    override fun invoke(): Flow<ItemCountSummary> = observePinnedItems(includeHidden = false)
        .map { items ->
            encryptionContextProvider.withEncryptionContext {
                items.toItemCountSummary(this)
            }
        }

    private fun List<Item>.toItemCountSummary(context: EncryptionContext): ItemCountSummary {
        var login = 0L
        var loginWithMFA = 0L
        var note = 0L
        var alias = 0L
        var creditCard = 0L
        var identities = 0L
        var custom = 0L

        forEach { item ->
            when (item.itemType.category) {
                ItemCategory.Login -> {
                    login++
                    if (item.hasPrimaryTotp(context)) {
                        loginWithMFA++
                    }
                }

                ItemCategory.Note -> note++
                ItemCategory.Alias -> alias++
                ItemCategory.CreditCard -> creditCard++
                ItemCategory.Identity -> identities++
                ItemCategory.Custom,
                ItemCategory.SSHKey,
                ItemCategory.WifiNetwork -> custom++

                ItemCategory.Password,
                ItemCategory.Unknown -> Unit
            }
        }

        return ItemCountSummary(
            login = login,
            loginWithMFA = loginWithMFA,
            note = note,
            alias = alias,
            creditCard = creditCard,
            identities = identities,
            custom = custom,
            sharedWithMe = 0,
            sharedByMe = 0,
            trashed = 0,
            sharedWithMeTrashed = 0
        )
    }

    private fun Item.hasPrimaryTotp(context: EncryptionContext): Boolean {
        val login = itemType as? ItemType.Login ?: return false
        return login.primaryTotp.isNotEmpty() && context.decrypt(login.primaryTotp).isNotBlank()
    }
}
