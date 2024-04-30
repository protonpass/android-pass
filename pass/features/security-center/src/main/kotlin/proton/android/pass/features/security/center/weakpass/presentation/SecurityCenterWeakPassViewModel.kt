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

package proton.android.pass.features.security.center.weakpass.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.domain.Item
import proton.android.pass.features.security.center.PassMonitorDisplayWeakPasswords
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class SecurityCenterWeakPassViewModel @Inject constructor(
    observeMonitoredItems: ObserveMonitoredItems,
    insecurePasswordChecker: InsecurePasswordChecker,
    userPreferencesRepository: UserPreferencesRepository,
    telemetryManager: TelemetryManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    init {
        telemetryManager.sendEvent(PassMonitorDisplayWeakPasswords)
    }

    internal val state: StateFlow<SecurityCenterWeakPassState> = combine(
        observeMonitoredItems(),
        userPreferencesRepository.getUseFaviconsPreference()
    ) { monitoredItems, useFavIconsPreference ->
        insecurePasswordChecker(monitoredItems).let { report ->
            SecurityCenterWeakPassState(
                vulnerablePasswordUiModels = report.vulnerablePasswordItems.toUiModels(),
                weakPasswordUiModels = report.weakPasswordItems.toUiModels(),
                isLoading = false,
                canLoadExternalImages = useFavIconsPreference.value()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterWeakPassState.Initial
    )

    private suspend fun List<Item>.toUiModels() = withContext(Dispatchers.Default) {
        encryptionContextProvider.withEncryptionContext {
            map { item ->
                item.toUiModel(this@withEncryptionContext).copy(isPinned = false)
            }
        }
    }

}
