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
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.aliascontacts.AliasContacts
import proton.android.pass.domain.items.ItemCategory

@Stable
sealed interface ItemDetailState {

    val itemContents: ItemContents

    val itemId: ItemId

    val shareId: ShareId

    val isItemPinned: Boolean

    val itemShare: Share

    val itemCategory: ItemCategory

    val itemCreatedAt: Instant

    val itemModifiedAt: Instant

    val itemLastAutofillAtOption: Option<Instant>

    val itemRevision: Long

    val itemState: ItemState

    val itemDiffs: ItemDiffs

    val itemShareCount: Int

    val attachmentsState: AttachmentsState

    val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>

    val detailEvent: DetailEvent

    fun update(itemContents: ItemContents, itemDiffs: ItemDiffs = ItemDiffs.None): ItemDetailState

    @Stable
    data class Alias(
        override val itemContents: ItemContents.Alias,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Alias,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent,
        val aliasDetails: AliasDetails,
        val aliasContacts: AliasContacts,
        val displayContactsBanner: Boolean,
        val isAliasStateToggling: Boolean
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Alias

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.Alias && itemDiffs is ItemDiffs.Alias -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.Alias -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.Alias -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }
    }

    @Stable
    data class CreditCard(
        override val itemContents: ItemContents.CreditCard,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.CreditCard,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.CreditCard

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.CreditCard && itemDiffs is ItemDiffs.CreditCard -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.CreditCard -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.CreditCard -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class Identity(
        override val itemContents: ItemContents.Identity,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Identity,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Identity

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.Identity && itemDiffs is ItemDiffs.Identity -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.Identity -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.Identity -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class Login(
        override val itemContents: ItemContents.Login,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Login,
        override val itemShareCount: Int,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val attachmentsState: AttachmentsState,
        override val detailEvent: DetailEvent,
        val canLoadExternalImages: Boolean,
        val passwordStrength: PasswordStrength,
        val primaryTotp: TotpState?,
        val passkeys: List<UIPasskeyContent>,
        val loginMonitorState: LoginMonitorState
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Login

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.Login && itemDiffs is ItemDiffs.Login -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.Login -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.Login -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class Custom(
        override val itemContents: ItemContents.Custom,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Custom,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Custom

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.Custom && itemDiffs is ItemDiffs.Custom -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.Custom -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.Custom -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class WifiNetwork(
        override val itemContents: ItemContents.WifiNetwork,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.WifiNetwork,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent,
        val svgQR: Option<String>
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.WifiNetwork

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.WifiNetwork && itemDiffs is ItemDiffs.WifiNetwork -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.WifiNetwork -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.WifiNetwork -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class SSHKey(
        override val itemContents: ItemContents.SSHKey,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.SSHKey,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.SSHKey

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.SSHKey && itemDiffs is ItemDiffs.SSHKey -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.SSHKey -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.SSHKey -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class Note(
        override val itemContents: ItemContents.Note,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Note,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Note

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = when {
            itemContents is ItemContents.Note && itemDiffs is ItemDiffs.Note -> this.copy(
                itemContents = itemContents,
                itemDiffs = itemDiffs
            )

            itemContents is ItemContents.Note -> this.copy(
                itemContents = itemContents
            )

            itemDiffs is ItemDiffs.Note -> this.copy(
                itemDiffs = itemDiffs
            )

            else -> this
        }

    }

    @Stable
    data class Unknown(
        override val itemContents: ItemContents.Unknown,
        override val itemId: ItemId,
        override val shareId: ShareId,
        override val isItemPinned: Boolean,
        override val itemShare: Share,
        override val itemCreatedAt: Instant,
        override val itemModifiedAt: Instant,
        override val itemLastAutofillAtOption: Option<Instant>,
        override val itemRevision: Long,
        override val itemState: ItemState,
        override val itemDiffs: ItemDiffs.Unknown,
        override val itemShareCount: Int,
        override val attachmentsState: AttachmentsState,
        override val customFieldTotps: Map<Pair<Option<Int>, Int>, TotpState>,
        override val detailEvent: DetailEvent
    ) : ItemDetailState {

        override val itemCategory: ItemCategory = ItemCategory.Unknown

        override fun update(itemContents: ItemContents, itemDiffs: ItemDiffs): ItemDetailState = this

    }

}

sealed interface DetailEvent {
    data object Idle : AliasDetailEvent
}
sealed interface AliasDetailEvent : DetailEvent {
    data class CreateLoginFromAlias(val alias: String, val shareId: ShareId) : AliasDetailEvent
    data class ContactSection(val shareId: ShareId, val itemId: ItemId) : AliasDetailEvent
}

