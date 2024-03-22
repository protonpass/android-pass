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

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel

@Stable
internal data class SecurityCenterReusedPassState(
    private val reusedPasswords: Map<Int, List<ItemUiModel>>,
    internal val canLoadExternalImages: Boolean
) {

    internal val reusedPassGroups: List<SecurityCenterReusedPassGroup> =
        reusedPasswords.map { (reusedPasswordsCount, itemUiModels) ->
            SecurityCenterReusedPassGroup(
                reusedPasswordsCount = reusedPasswordsCount,
                itemUiModels = itemUiModels
            )
        }

    internal companion object {

        internal val Initial: SecurityCenterReusedPassState = SecurityCenterReusedPassState(
            reusedPasswords = emptyMap(),
            canLoadExternalImages = false
        )

    }

}

internal data class SecurityCenterReusedPassGroup(
    internal val reusedPasswordsCount: Int,
    internal val itemUiModels: List<ItemUiModel>
)
