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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContacts
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class DetailAliasContactViewModel @Inject constructor(
    observeAliasDetails: ObserveAliasDetails,
    observeAliasContacts: ObserveAliasContacts,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val detailAliasContactEventFlow: MutableStateFlow<DetailAliasContactEvent> =
        MutableStateFlow(DetailAliasContactEvent.Idle)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val contactsFlow = observeAliasContacts(shareId, itemId, fullList = true)
        .mapLatest { it.contacts.partition { contact -> contact.blocked } }
        .distinctUntilChanged()
        .asLoadingResult()

    val state = combine(
        detailAliasContactEventFlow,
        observeAliasDetails(shareId, itemId).asLoadingResult(),
        contactsFlow
    ) { event, aliasDetailsResult, aliasContactsResult ->
        val aliasDetails = aliasDetailsResult.getOrNull()
        val emptyPair = emptyList<Contact>() to emptyList<Contact>()
        val (blockedContacts, forwardingContacts) = aliasContactsResult.getOrNull() ?: emptyPair

        DetailAliasContactUIState(
            event = event,
            senderName = aliasDetails?.name.orEmpty(),
            displayName = aliasDetails?.displayName.orEmpty(),
            forwardingContacts = forwardingContacts.toPersistentList(),
            blockedContacts = blockedContacts.toPersistentList()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = DetailAliasContactUIState.Empty
        )

    fun onCreateItem() {
        detailAliasContactEventFlow.update { DetailAliasContactEvent.CreateItem(shareId, itemId) }
    }

    fun onEventConsumed(event: DetailAliasContactEvent) {
        detailAliasContactEventFlow.compareAndSet(event, DetailAliasContactEvent.Idle)
    }
}

sealed interface DetailAliasContactEvent {
    data object Idle : DetailAliasContactEvent
    data class CreateItem(val shareId: ShareId, val itemId: ItemId) : DetailAliasContactEvent
}
