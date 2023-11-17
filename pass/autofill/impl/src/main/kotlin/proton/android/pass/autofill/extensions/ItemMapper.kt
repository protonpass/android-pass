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

package proton.android.pass.autofill.extensions

import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId

fun ItemUiModel.toAutoFillItem(): Option<AutofillItem> =
    if (contents is ItemContents.Login) {
        val asLogin = contents as ItemContents.Login
        AutofillItem(
            shareId = shareId.id,
            itemId = id.id,
            username = asLogin.username,
            password = asLogin.password.encrypted,
            totp = asLogin.primaryTotp.encrypted
        ).toOption()
    } else {
        None
    }

fun Item.toAutofillItem(): Option<AutofillItem> =
    if (itemType is ItemType.Login) {
        val asLogin = itemType as ItemType.Login
        AutofillItem(
            shareId = shareId.id,
            itemId = id.id,
            username = asLogin.username,
            password = asLogin.password,
            totp = asLogin.primaryTotp
        ).toOption()
    } else {
        None
    }

data class CreatedAlias(
    val shareId: ShareId,
    val itemId: ItemId,
    val alias: String
)

fun CreatedAlias.toAutofillItem(): AutofillItem =
    AutofillItem(
        shareId = shareId.id,
        itemId = itemId.id,
        username = alias,
        password = null,
        totp = null
    )
