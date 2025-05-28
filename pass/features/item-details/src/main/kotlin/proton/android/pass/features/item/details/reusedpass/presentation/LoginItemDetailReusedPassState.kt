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

package proton.android.pass.features.item.details.reusedpass.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault

@Stable
internal data class LoginItemDetailReusedPassState(
    internal val password: String,
    internal val duplicatedPasswordLoginItems: ImmutableList<ItemUiModel>,
    internal val canLoadExternalImages: Boolean,
    internal val isLoadingState: IsLoadingState,
    private val groupedVaults: Map<ShareId, Vault>
) {

    internal fun getShareIcon(shareId: ShareId): ShareIcon? = groupedVaults[shareId]?.icon

    internal companion object {

        internal val Initial: LoginItemDetailReusedPassState = LoginItemDetailReusedPassState(
            password = "",
            duplicatedPasswordLoginItems = persistentListOf(),
            canLoadExternalImages = false,
            isLoadingState = IsLoadingState.Loading,
            groupedVaults = emptyMap()
        )

    }

}
