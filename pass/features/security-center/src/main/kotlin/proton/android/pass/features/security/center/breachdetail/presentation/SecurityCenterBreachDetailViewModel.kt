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

package proton.android.pass.features.security.center.breachdetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.shared.navigation.BreachEmailIdArgId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SecurityCenterBreachDetailViewModel @Inject constructor(
    observeBreachesForCustomEmail: ObserveBreachesForCustomEmail,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val breachId: BreachId = savedStateHandleProvider.get()
        .require<String>(BreachIdArgId.key)
        .let(::BreachId)
    private val customEmailId: BreachCustomEmailId? = savedStateHandleProvider.get()
        .get<String>(BreachEmailIdArgId.key)
        ?.let(::BreachCustomEmailId)
    private val shareId: ShareId? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.ShareId.key)
        ?.let(::ShareId)
    private val itemId: ItemId? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.ItemId.key)
        ?.let(::ItemId)

    private val observeBreachForEmailFlow = when {
        customEmailId != null -> observeBreachesForCustomEmail(id = customEmailId)
        shareId != null && itemId != null -> observeBreachesForAliasEmail(
            shareId = shareId,
            itemId = itemId
        )

        else -> emptyFlow()
    }
        .map { it.firstOrNull { breach -> breach.id == breachId.id } }
        .asLoadingResult()
        .distinctUntilChanged()

    private val isLoadingStateFlow = MutableStateFlow(false)
    private val eventFlow =
        MutableStateFlow<SecurityCenterBreachDetailEvent>(SecurityCenterBreachDetailEvent.Idle)

    internal val state: StateFlow<SecurityCenterBreachDetailState> = combine(
        observeBreachForEmailFlow,
        eventFlow,
        isLoadingStateFlow
    ) { breachResult, event, isLoading ->
        SecurityCenterBreachDetailState(
            breachEmail = breachResult.getOrNull(),
            event = event,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterBreachDetailState.Initial
    )

    internal fun onEventConsumed(event: SecurityCenterBreachDetailEvent) {
        eventFlow.compareAndSet(event, SecurityCenterBreachDetailEvent.Idle)
    }
}
