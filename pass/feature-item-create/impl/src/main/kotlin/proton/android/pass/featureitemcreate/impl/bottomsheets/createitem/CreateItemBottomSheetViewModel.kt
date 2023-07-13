/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class CreateItemBottomSheetViewModel @Inject constructor(
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val navShareId: Option<ShareId> =
        savedStateHandle.get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }

    val state: StateFlow<CreateItemBottomSheetUIState> = observeUpgradeInfo()
        .map { upgradeInfo ->
            CreateItemBottomSheetUIState(
                shareId = navShareId.value(),
                createItemAliasUIState = CreateItemAliasUIState(
                    canUpgrade = upgradeInfo.isUpgradeAvailable,
                    aliasCount = upgradeInfo.totalAlias,
                    aliasLimit = upgradeInfo.plan.aliasLimit.limitOrNull() ?: 0
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CreateItemBottomSheetUIState.DEFAULT
        )

}

data class CreateItemBottomSheetUIState(
    val shareId: ShareId?,
    val createItemAliasUIState: CreateItemAliasUIState
) {
    companion object {
        val DEFAULT = CreateItemBottomSheetUIState(
            shareId = null,
            createItemAliasUIState = CreateItemAliasUIState.DEFAULT
        )
    }
}

data class CreateItemAliasUIState(
    val canUpgrade: Boolean,
    val aliasCount: Int,
    val aliasLimit: Int
) {
    companion object {
        val DEFAULT = CreateItemAliasUIState(
            canUpgrade = false,
            aliasCount = 0,
            aliasLimit = 0
        )
    }
}
