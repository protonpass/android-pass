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

package proton.android.pass.features.security.center.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.MarkEmailBreachAsResolved
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForProtonEmail
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.report.navigation.BreachCountIdArgId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.features.security.center.shared.navigation.EmailArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class SecurityCenterReportViewModel @Inject constructor(
    observeBreachesForCustomEmail: ObserveBreachesForCustomEmail,
    observeBreachesForProtonEmail: ObserveBreachesForProtonEmail,
    observeBreachesForAliasEmail: ObserveBreachesForAliasEmail,
    observeItems: ObserveItems,
    private val markEmailBreachAsResolved: MarkEmailBreachAsResolved,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val customEmailId: BreachEmailId.Custom? = savedStateHandleProvider.get()
        .get<String>(BreachIdArgId.key)
        ?.let { BreachEmailId.Custom(BreachId(it)) }

    private val aliasEmailId: BreachEmailId.Alias? = run {
        val shareId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ShareId.key)
            ?.let(::ShareId)
        val itemId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ItemId.key)
            ?.let(::ItemId)
        if (shareId != null && itemId != null) {
            BreachEmailId.Alias(BreachId(""), shareId, itemId)
        } else {
            null
        }
    }

    private val protonEmailId: BreachEmailId.Proton? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.AddressId.key)
        ?.let { BreachEmailId.Proton(BreachId(""), AddressId(it)) }

    private val email: String = savedStateHandleProvider.get()
        .require<String>(EmailArgId.key)
        .let(NavParamEncoder::decode)

    private val breachCount: Int = savedStateHandleProvider.get()
        .require(BreachCountIdArgId.key)

    private val emailType: BreachEmailId by lazy {
        when {
            protonEmailId != null -> protonEmailId
            aliasEmailId != null -> aliasEmailId
            customEmailId != null -> customEmailId
            else -> throw IllegalStateException("Invalid state")
        }
    }

    private val observeBreachForEmailFlow = when (val type = emailType) {
        is BreachEmailId.Alias -> observeBreachesForAliasEmail(
            shareId = type.shareId,
            itemId = type.itemId
        )

        is BreachEmailId.Custom -> observeBreachesForCustomEmail(id = type)
        is BreachEmailId.Proton -> observeBreachesForProtonEmail(addressId = type.addressId)
        else -> throw IllegalStateException("Invalid state")
    }
        .asLoadingResult()
        .distinctUntilChanged()

    private val usedInLoginItemsFlow = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Logins
    )
        .map { items ->
            items.mapNotNull { item ->
                item.takeIf { (item.itemType as ItemType.Login).username == email }
            }.let {
                encryptionContextProvider.withEncryptionContext {
                    it.map { item -> item.toUiModel(this) }
                }
            }
        }
        .asLoadingResult()
        .distinctUntilChanged()

    internal val state: StateFlow<SecurityCenterReportState> = combine(
        observeBreachForEmailFlow,
        usedInLoginItemsFlow,
        userPreferencesRepository.getUseFaviconsPreference()
    ) { breachesForEmailResult, usedInLoginItemsResult, useFavIconsPreference ->
        val isBreachesLoading = breachesForEmailResult is LoadingResult.Loading
        val isUsedInLoading = usedInLoginItemsResult is LoadingResult.Loading
        val breaches = breachesForEmailResult.getOrNull() ?: persistentListOf()
        SecurityCenterReportState(
            breachCount = breachCount,
            usedInItems = usedInLoginItemsResult.getOrNull()
                ?.toImmutableList()
                ?: persistentListOf(),
            email = email,
            canLoadExternalImages = useFavIconsPreference.value(),
            breachEmails = breaches,
            isLoading = isBreachesLoading || isUsedInLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterReportState.default(email, breachCount)
    )

    fun resolveEmailBreach(emailId: BreachEmailId) {
        viewModelScope.launch {
            runCatching { markEmailBreachAsResolved(breachEmailId = emailId) }
                .onSuccess {
                }
                .onError { }
        }
    }
}
