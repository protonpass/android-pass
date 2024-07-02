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

package proton.android.pass.commonuimodels.api.items

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Totp
import proton.android.pass.domain.Vault
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.items.ItemCustomField

@Stable
sealed interface ItemDetailState {

    val itemContents: ItemContents

    val itemId: ItemId

    val shareId: ShareId

    val isItemPinned: Boolean

    val itemVault: Vault?

    val itemCategory: ItemCategory

    val itemCreatedAt: Instant

    val itemModifiedAt: Instant

    @Stable
    data class Alias(
        override val itemContents: ItemContents.Alias,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        val mailboxes: List<AliasMailbox>
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Alias

    }

    @Stable
    data class CreditCard(
        override val itemContents: ItemContents.CreditCard,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.CreditCard

    }

    @Stable
    data class Identity(
        override val itemContents: ItemContents.Identity,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        val itemLastAutofillAtOption: Option<Instant>,
        val itemRevision: Long
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Identity

    }

    @Stable
    data class Login(
        override val itemContents: ItemContents.Login,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        val canLoadExternalImages: Boolean,
        val passwordStrength: PasswordStrength,
        val primaryTotp: Totp?,
        val customFields: List<ItemCustomField>,
        val passkeys: List<UIPasskeyContent>,
        val isUsernameSplitEnabled: Boolean
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Login

    }

    @Stable
    data class Note(
        override val itemContents: ItemContents.Note,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Note

    }

    @Stable
    data class Unknown(
        override val itemContents: ItemContents.Unknown,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemVault: Vault?,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Unknown

    }

}
