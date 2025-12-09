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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForProtonEmail
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.darkweb.navigation.CustomEmailNavArgId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SecurityCenterBreachDetailViewModel @Inject constructor(
    observeBreachesForCustomEmail: ObserveBreachesForCustomEmail,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeBreachesForProtonEmail: ObserveBreachesForProtonEmail,
    savedStateHandleProvider: SavedStateHandleProvider,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val selectedBreachId: BreachId = savedStateHandleProvider.get()
        .require<String>(BreachIdArgId.key)
        .let(::BreachId)
        .also {
            PassLogger.i(TAG, "Selected breach id: $it")
        }

    private val customEmailId: BreachEmailId.Custom? = run {
        val customEmailId: CustomEmailId? = savedStateHandleProvider.get()
            .get<String>(CustomEmailNavArgId.key)
            ?.let(::CustomEmailId)

        if (customEmailId != null) {
            BreachEmailId.Custom(id = selectedBreachId, customEmailId = customEmailId)
        } else null
    }

    private val aliasEmailId: BreachEmailId.Alias? = run {
        val shareId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ShareId.key)
            ?.let(::ShareId)
        val itemId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ItemId.key)
            ?.let(::ItemId)
        if (shareId != null && itemId != null) {
            BreachEmailId.Alias(selectedBreachId, shareId, itemId)
        } else {
            null
        }
    }
    private val protonEmailId: BreachEmailId.Proton? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.AddressId.key)
        ?.let { BreachEmailId.Proton(selectedBreachId, AddressId(it)) }

    private val emailType: BreachEmailId by lazy {
        when {
            protonEmailId != null -> protonEmailId
            aliasEmailId != null -> aliasEmailId
            customEmailId != null -> customEmailId
            else -> throw IllegalStateException("Invalid email type")
        }.also {
            PassLogger.i(TAG, "BreachEmailId: $it")
        }
    }

    private val observeBreachForEmailFlow = when (val type = emailType) {
        is BreachEmailId.Alias -> observeBreachesForAliasEmail(
            shareId = type.shareId,
            itemId = type.itemId
        )

        is BreachEmailId.Custom -> observeBreachesForCustomEmail(
            id = type.customEmailId
        )
        is BreachEmailId.Proton -> observeBreachesForProtonEmail(addressId = type.addressId)
    }.take(1).map {
        it.firstOrNull { breach -> breach.emailId.id == selectedBreachId }
    }.asLoadingResult().distinctUntilChanged()

    private val isLoadingStateFlow = MutableStateFlow(false)

    internal val state: StateFlow<SecurityCenterBreachDetailState> = combine(
        observeBreachForEmailFlow,
        isLoadingStateFlow
    ) { breachResult, isLoading ->

        when (breachResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error loading breach information")
                PassLogger.w(TAG, breachResult.exception)

                snackbarDispatcher(SecurityCenterBreachSnackbarMessage.GetBreachDetailsError)
                SecurityCenterBreachDetailState(
                    breachEmail = null,
                    isLoading = false,
                    event = SecurityCenterBreachDetailEvent.Close
                )
            }

            is LoadingResult.Loading -> {
                SecurityCenterBreachDetailState(
                    breachEmail = null,
                    isLoading = true,
                    event = SecurityCenterBreachDetailEvent.Idle
                )
            }

            is LoadingResult.Success -> {
                SecurityCenterBreachDetailState(
                    breachEmail = breachResult.data,
                    isLoading = isLoading,
                    event = SecurityCenterBreachDetailEvent.Idle
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterBreachDetailState.Initial
    )

    companion object {
        private const val TAG = "SecurityCenterBreachDetailViewModel"
    }
}
