/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.attachments.storagefull.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import javax.inject.Inject

@HiltViewModel
class StorageFullViewmodel @Inject constructor(
    observeUserAccessData: ObserveUserAccessData,
    observeUpgradeInfo: ObserveUpgradeInfo
) : ViewModel() {

    private val upgradeInfoFlow = observeUpgradeInfo().asLoadingResult()

    val state = combine(
        observeUserAccessData().filterNotNull(),
        upgradeInfoFlow
    ) { userData, upgradeInfoResult ->
        val upgradeInfo = upgradeInfoResult.getOrNull()
        val isUpgradeAvailable = upgradeInfo?.isUpgradeAvailable ?: false

        StorageFullState.Success(
            used = userData.storageUsed,
            quota = userData.storageQuota,
            canUpgrade = isUpgradeAvailable
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = StorageFullState.Loading
    )
}
