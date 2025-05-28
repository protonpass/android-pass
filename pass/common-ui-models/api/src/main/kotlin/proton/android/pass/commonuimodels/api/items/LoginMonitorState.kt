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

package proton.android.pass.commonuimodels.api.items

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonuimodels.api.ItemUiModel

@Stable
data class LoginMonitorState(
    internal val isExcludedFromMonitor: Boolean,
    private val navigationScope: ItemDetailNavScope,
    val isPasswordInsecure: Boolean,
    val isPasswordReused: Boolean,
    val isMissingTwoFa: Boolean,
    val reusedPasswordDisplayMode: ReusedPasswordDisplayMode,
    val reusedPasswordCount: Int,
    val reusedPasswordItems: ImmutableList<ItemUiModel>
) {

    enum class ReusedPasswordDisplayMode {
        Compact,
        Expanded
    }

    val shouldDisplayMonitoring: Boolean = when (navigationScope) {
        ItemDetailNavScope.Default -> false

        ItemDetailNavScope.MonitorExcluded,
        ItemDetailNavScope.MonitorReport,
        ItemDetailNavScope.MonitorWeakPassword,
        ItemDetailNavScope.MonitorMissing2fa,
        ItemDetailNavScope.MonitorReusedPassword -> true

    }
}
