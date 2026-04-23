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
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.LoginTotpEntry
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveLoginTotpEntries
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveLoginTotpEntriesImpl @Inject constructor(
    private val observeItems: ObserveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveLoginTotpEntries {

    override fun invoke(): Flow<List<LoginTotpEntry>> = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Logins,
        includeHidden = false
    ).map { items ->
        encryptionContextProvider.withEncryptionContext {
            items.flatMap { item -> item.toTotpEntries(this) }
        }
    }

    private fun Item.toTotpEntries(context: EncryptionContext): List<LoginTotpEntry> {
        val login = itemType as? ItemType.Login ?: return emptyList()
        val itemTitle = context.decrypt(title)
        val primarySubtitle = login.itemEmail.ifBlank { login.itemUsername }
        val packageName = packageInfoSet.firstOrNull()?.packageName?.value

        val entries = mutableListOf<LoginTotpEntry>()
        if (login.primaryTotp.isNotEmpty()) {
            val uri = context.decrypt(login.primaryTotp)
            if (uri.isNotEmpty()) {
                entries += LoginTotpEntry(
                    shareId = shareId,
                    itemId = id,
                    itemTitle = itemTitle,
                    subtitle = primarySubtitle,
                    websites = login.websites,
                    packageName = packageName,
                    totpUri = uri,
                    source = LoginTotpEntry.Source.Primary
                )
            }
        }
        login.customFields.forEach { field ->
            if (field is CustomField.Totp && field.value.isNotEmpty()) {
                val uri = context.decrypt(field.value)
                if (uri.isNotEmpty()) {
                    entries += LoginTotpEntry(
                        shareId = shareId,
                        itemId = id,
                        itemTitle = itemTitle,
                        subtitle = field.label,
                        websites = login.websites,
                        packageName = packageName,
                        totpUri = uri,
                        source = LoginTotpEntry.Source.CustomField
                    )
                }
            }
        }
        return entries
    }
}
