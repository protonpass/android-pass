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

import proton.android.pass.commonui.api.itemName
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.log.api.PassLogger

object ItemDisplayBuilder {

    private const val TAG = "ItemDisplayBuilder"

    fun createTitle(
        item: Item,
        encryptionContext: EncryptionContext
    ): String = when (item.itemType) {
        is ItemType.CreditCard -> item.itemName(encryptionContext)
        is ItemType.Login -> item.itemName(encryptionContext)
        else -> {
            PassLogger.w(TAG, "Unsupported item type: ${item.itemType::class.java}")
            throw IllegalStateException("Unsupported item type")
        }
    }

    fun createSubtitle(
        item: Item,
        encryptionContext: EncryptionContext
    ): String = when (val itemType = item.itemType) {
        is ItemType.CreditCard -> createCreditCardSubtitle(encryptionContext, itemType)
        is ItemType.Login -> itemType.username.takeIf { it.isNotBlank() } ?: "---"
        else -> {
            PassLogger.w(TAG, "Unsupported item type: ${item.itemType::class.java}")
            throw IllegalStateException("Unsupported item type")
        }
    }

    private fun createCreditCardSubtitle(
        encryptionContext: EncryptionContext,
        itemType: ItemType.CreditCard
    ): String {
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
        return listOfNotNull(formattedNumber, formattedDate).joinToString(" • ")
    }
}
