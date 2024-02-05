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

package proton.android.pass.commonui.api

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.datamodels.api.toContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType

fun Item.toUiModel(context: EncryptionContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        contents = toItemContents(context),
        state = state,
        createTime = createTime,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime.value(),
        isPinned = isPinned,
    )

fun Item.itemName(context: EncryptionContext): String =
    context.decrypt(title)

fun Item.toItemContents(encryptionContext: EncryptionContext): ItemContents =
    when (val type = itemType) {
        is ItemType.Alias -> ItemContents.Alias(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note),
            aliasEmail = type.aliasEmail
        )

        is ItemType.Login -> ItemContents.Login(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note),
            username = type.username,
            password = concealedOrEmpty(type.password, encryptionContext),
            urls = type.websites,
            packageInfoSet = type.packageInfoSet,
            primaryTotp = concealedOrEmpty(type.primaryTotp, encryptionContext),
            customFields = type.customFields.mapNotNull { it.toContent(encryptionContext, true) },
            passkeys = type.passkeys,
        )

        is ItemType.Note -> ItemContents.Note(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note)
        )

        is ItemType.CreditCard -> ItemContents.CreditCard(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note),
            type = type.creditCardType,
            cardHolder = type.cardHolder,
            number = encryptionContext.decrypt(type.number),
            cvv = concealedOrEmpty(type.cvv, encryptionContext),
            pin = concealedOrEmpty(type.pin, encryptionContext),
            expirationDate = type.expirationDate,
        )

        ItemType.Password,
        ItemType.Unknown -> ItemContents.Unknown(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note)
        )
    }

private fun concealedOrEmpty(value: String, encryptionContext: EncryptionContext): HiddenState {
    val asByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
    return if (asByteArray.isEmpty()) {
        HiddenState.Empty(value)
    } else {
        HiddenState.Concealed(value)
    }
}
