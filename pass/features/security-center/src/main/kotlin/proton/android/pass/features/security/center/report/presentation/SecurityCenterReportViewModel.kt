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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.features.security.center.report.navigation.BreachesArgId
import proton.android.pass.features.security.center.report.navigation.EmailType
import proton.android.pass.features.security.center.report.navigation.EmailTypeArgId
import proton.android.pass.features.security.center.shared.navigation.BreachEmailIdArgId
import proton.android.pass.features.security.center.shared.navigation.EmailArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class SecurityCenterReportViewModel @Inject constructor(
    observeBreachesForCustomEmail: ObserveBreachesForCustomEmail,
    observeItems: ObserveItems,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val emailType: EmailType = savedStateHandleProvider.get()
        .require(EmailTypeArgId.key)

    private val id: BreachCustomEmailId = savedStateHandleProvider.get()
        .require<String>(BreachEmailIdArgId.key)
        .let(::BreachCustomEmailId)

    private val email: String = savedStateHandleProvider.get()
        .require<String>(EmailArgId.key)
        .let(NavParamEncoder::decode)

    private val breachCount: Int = savedStateHandleProvider.get()
        .require(BreachesArgId.key)

    private val breachesForEmailFlow =
        when (emailType) {
            EmailType.Custom -> observeBreachesForCustomEmail(id = id).asLoadingResult()
            else -> throw IllegalArgumentException("Unsupported email type: $emailType")
        }
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
        breachesForEmailFlow,
        usedInLoginItemsFlow,
        userPreferencesRepository.getUseFaviconsPreference()
    ) { breachesForEmailResult, usedInLoginItemsResult, useFavIconsPreference ->
        val isBreachesLoading = breachesForEmailResult is LoadingResult.Loading
        val isUsedInLoading = usedInLoginItemsResult is LoadingResult.Loading
        SecurityCenterReportState(
            breachCount = breachCount,
            usedInItems = usedInLoginItemsResult.getOrNull()
                ?.toImmutableList()
                ?: persistentListOf(),
            email = email,
            canLoadExternalImages = useFavIconsPreference.value(),
            isLoading = isBreachesLoading || isUsedInLoading
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterReportState.default(email, breachCount)
    )
}
