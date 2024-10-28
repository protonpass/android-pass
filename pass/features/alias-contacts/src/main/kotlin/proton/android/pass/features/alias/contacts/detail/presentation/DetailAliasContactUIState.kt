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

package proton.android.pass.features.alias.contacts.detail.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId

data class DetailAliasContactUIState(
    val shareId: Option<ShareId>,
    val itemId: Option<ItemId>,
    val event: DetailAliasContactEvent,
    val displayName: String,
    val aliasContactsListUIState: AliasContactsListUIState,
    val contactBlockIsLoading: PersistentSet<ContactId>,
    val hasShownAliasContactsOnboarding: Boolean,
    val canManageContacts: Boolean
) {

    companion object {
        val Empty = DetailAliasContactUIState(
            shareId = None,
            itemId = None,
            event = DetailAliasContactEvent.Idle,
            displayName = "",
            aliasContactsListUIState = AliasContactsListUIState.Empty,
            contactBlockIsLoading = persistentSetOf(),
            hasShownAliasContactsOnboarding = true,
            canManageContacts = false
        )
    }
}

data class AliasContactsListUIState(
    val forwardingContacts: ImmutableList<Contact>,
    val blockedContacts: ImmutableList<Contact>,
    val isLoading: Boolean
) {
    val hasContacts = forwardingContacts.isNotEmpty() || blockedContacts.isNotEmpty()

    companion object {
        val Empty = AliasContactsListUIState(
            forwardingContacts = persistentListOf(),
            blockedContacts = persistentListOf(),
            isLoading = false
        )
    }
}
