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
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Totp
import proton.android.pass.domain.Vault
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.items.ItemCustomField

@Stable
sealed class ItemDetailState(
    val itemContents: ItemContents,
    val isItemPinned: Boolean,
    val itemVault: Vault?,
    val itemCategory: ItemCategory
) {

    @Stable
    data class Alias(
        val contents: ItemContents.Alias,
        val mailboxes: List<AliasMailbox>,
        private val isPinned: Boolean,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = isPinned,
        itemVault = vault,
        itemCategory = ItemCategory.Alias
    )

    @Stable
    data class CreditCard(
        val contents: ItemContents.CreditCard,
        private val isPinned: Boolean,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = isPinned,
        itemVault = vault,
        itemCategory = ItemCategory.CreditCard
    )

    @Stable
    data class Identity(
        val contents: ItemContents.Identity,
        val createdAt: Instant,
        val modifiedAt: Instant,
        private val isPinned: Boolean,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = isPinned,
        itemVault = vault,
        itemCategory = ItemCategory.Identity
    )

    @Stable
    data class Login(
        val contents: ItemContents.Login,
        val canLoadExternalImages: Boolean,
        val passwordStrength: PasswordStrength,
        val primaryTotp: Totp?,
        val customFields: List<ItemCustomField>,
        val passkeys: List<UIPasskeyContent>,
        val isUsernameSplitEnabled: Boolean,
        private val isPinned: Boolean,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = isPinned,
        itemVault = vault,
        itemCategory = ItemCategory.Login
    )

    @Stable
    data class Note(
        val contents: ItemContents.Note,
        private val isPinned: Boolean,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = isPinned,
        itemVault = vault,
        itemCategory = ItemCategory.Note
    )

    @Stable
    data class Unknown(
        val contents: ItemContents.Unknown,
        private val vault: Vault?
    ) : ItemDetailState(
        itemContents = contents,
        isItemPinned = false,
        itemVault = vault,
        itemCategory = ItemCategory.Unknown
    )

}
