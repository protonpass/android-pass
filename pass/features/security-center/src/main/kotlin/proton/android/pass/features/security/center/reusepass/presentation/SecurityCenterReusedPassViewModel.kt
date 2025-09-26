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

package proton.android.pass.features.security.center.reusepass.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.Item
import proton.android.pass.features.security.center.PassMonitorDisplayReusedPasswords
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordChecker
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class SecurityCenterReusedPassViewModel @Inject constructor(
    observeMonitoredItems: ObserveMonitoredItems,
    observeVaultsGroupedByShareId: ObserveVaultsGroupedByShareId,
    repeatedPasswordChecker: RepeatedPasswordChecker,
    userPreferencesRepository: UserPreferencesRepository,
    telemetryManager: TelemetryManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayReusedPasswords)
    }

    internal val state: StateFlow<SecurityCenterReusedPassState> =
        combine(
            observeMonitoredItems(includeHiddenVaults = false),
            userPreferencesRepository.getUseFaviconsPreference(),
            observeVaultsGroupedByShareId(includeHidden = false)
        ) { monitoredItems, useFavIconsPreference, groupedVaults ->

            val reusedPasswords = repeatedPasswordChecker(monitoredItems)

            SecurityCenterReusedPassState(
                reusedPasswords = reusedPasswords.repeatedPasswordsGroups.map { group ->
                    SecurityCenterReusedPassGroup(
                        key = group.password,
                        reusedPasswordsCount = group.count,
                        itemUiModels = group.items.toUiModels().toPersistentList()
                    )
                }.toPersistentList(),
                isLoading = false,
                canLoadExternalImages = useFavIconsPreference.value(),
                groupedVaults = groupedVaults
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SecurityCenterReusedPassState.Initial
        )

    private suspend fun List<Item>.toUiModels() = withContext(Dispatchers.Default) {
        encryptionContextProvider.withEncryptionContext {
            map { item ->
                item.toUiModel(this@withEncryptionContext).copy(isPinned = false)
            }
        }
    }

}
