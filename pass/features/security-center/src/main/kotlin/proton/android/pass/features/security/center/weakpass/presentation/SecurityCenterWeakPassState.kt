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

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.ItemUiModel

@Stable
internal data class SecurityCenterWeakPassState(
    private val vulnerablePasswordUiModels: List<ItemUiModel>,
    private val weakPasswordUiModels: List<ItemUiModel>,
    internal val canLoadExternalImages: Boolean
) {

    internal val weakPassGroups: ImmutableList<SecurityCenterWeakPassGroup> =
        mutableListOf<SecurityCenterWeakPassGroup>().apply {
            if (vulnerablePasswordUiModels.isNotEmpty()) {
                add(
                    SecurityCenterWeakPassGroup(
                        passwordStrength = PasswordStrength.Vulnerable,
                        itemUiModels = vulnerablePasswordUiModels
                    )
                )
            }
            if (weakPasswordUiModels.isNotEmpty()) {
                add(
                    SecurityCenterWeakPassGroup(
                        passwordStrength = PasswordStrength.Weak,
                        itemUiModels = weakPasswordUiModels
                    )
                )
            }
        }.toPersistentList()

    internal companion object {

        internal val Initial = SecurityCenterWeakPassState(
            vulnerablePasswordUiModels = emptyList(),
            weakPasswordUiModels = emptyList(),
            canLoadExternalImages = false
        )

    }

}

internal data class SecurityCenterWeakPassGroup(
    internal val passwordStrength: PasswordStrength,
    internal val itemUiModels: List<ItemUiModel>
)
