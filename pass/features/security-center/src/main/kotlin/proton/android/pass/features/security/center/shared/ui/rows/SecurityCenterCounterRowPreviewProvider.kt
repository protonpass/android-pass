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

package proton.android.pass.features.security.center.shared.ui.rows

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class SecurityCenterCounterRowPreviewProvider : PreviewParameterProvider<SecurityCenterCounterRowModel> {
    override val values: Sequence<SecurityCenterCounterRowModel>
        get() = sequenceOf(
            SecurityCenterCounterRowModel.Alert(
                title = "Alert title",
                subtitle = "Alert subtitle",
                count = 1
            ),
            SecurityCenterCounterRowModel.Success(
                title = "Success title",
                subtitle = "Success subtitle"
            ),
            SecurityCenterCounterRowModel.Indicator(
                title = "Indicator title",
                subtitle = "Indicator subtitle",
                count = null
            ),
            SecurityCenterCounterRowModel.Indicator(
                title = "Indicator title",
                subtitle = "Indicator subtitle",
                count = 1
            ),
            SecurityCenterCounterRowModel.Standard(
                title = "Standard title",
                subtitle = "Standard subtitle",
                count = null,
                showPassPlusIcon = false
            ),
            SecurityCenterCounterRowModel.Standard(
                title = "Standard title",
                subtitle = "Standard subtitle",
                count = 1,
                showPassPlusIcon = true
            ),
            SecurityCenterCounterRowModel.Loading(
                title = "Loading title"
            )
        )
}
