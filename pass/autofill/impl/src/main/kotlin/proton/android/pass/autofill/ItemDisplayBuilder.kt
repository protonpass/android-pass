/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill

import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.itemName
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType

internal object ItemDisplayBuilder {

    private const val EMPTY_SUBTITLE = "---"

    internal fun createTitle(item: Item, encryptionContext: EncryptionContext): String = when (item.itemType) {
        is ItemType.CreditCard,
        is ItemType.Login,
        is ItemType.Identity -> item.itemName(encryptionContext)
        is ItemType.Alias,
        is ItemType.Note,
        is ItemType.WifiNetwork,
        is ItemType.SSHKey,
        is ItemType.Custom,
        ItemType.Password,
        ItemType.Unknown -> throw IllegalArgumentException("Unsupported item type for title")
    }

    internal fun createSubtitle(item: Item, encryptionContext: EncryptionContext): String =
        when (val itemType = item.itemType) {
            is ItemType.CreditCard -> createCreditCardSubtitle(encryptionContext, itemType)
            is ItemType.Login -> createLoginSubtitle(itemType)
            is ItemType.Identity -> createIdentitySubtitle(itemType)
            is ItemType.Alias,
            is ItemType.Note,
            is ItemType.WifiNetwork,
            is ItemType.SSHKey,
            is ItemType.Custom,
            ItemType.Password,
            ItemType.Unknown -> throw IllegalArgumentException("Unsupported item type for subtitle")
        }

    private fun createIdentitySubtitle(itemType: ItemType.Identity): String =
        itemType.personalDetails.fullName.ifBlank { EMPTY_SUBTITLE }

    private fun createLoginSubtitle(itemType: ItemType.Login): String = when {
        itemType.itemUsername.isNotBlank() -> itemType.itemUsername
        itemType.itemEmail.isNotBlank() -> itemType.itemEmail
        else -> EMPTY_SUBTITLE
    }

    private fun createCreditCardSubtitle(encryptionContext: EncryptionContext, itemType: ItemType.CreditCard): String {
        val decryptedNumber = encryptionContext.decrypt(itemType.number)
        val cleanNumber = decryptedNumber.replace(" ", "")
        val formattedNumber = when {
            decryptedNumber.length <= 4 -> cleanNumber
            decryptedNumber.length > 4 -> "**** ${cleanNumber.takeLast(4)}"
            else -> null
        }

        val date = itemType.expirationDate.split('-')
        val formattedDate = if (date.size == 2) {
            "${date.last()}/${date.first().takeLast(2)}"
        } else {
            null
        }
        return listOfNotNull(formattedNumber, formattedDate).joinToString(" ${SpecialCharacters.DOT_SEPARATOR} ")
    }
}
