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

package proton.android.pass.features.security.center.darkweb.ui.customemails.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiState
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiStatus

private class CustomEmailItemPreviewProvider : PreviewParameterProvider<CustomEmailUiState> {
    override val values: Sequence<CustomEmailUiState>
        get() = sequenceOf(
            CustomEmailUiState(
                email = "verified@email.address",
                status = CustomEmailUiStatus.Verified(
                    id = CustomEmailId("1"),
                    breachesDetected = 2
                )
            ),
            CustomEmailUiState(
                email = "unverified@email.address",
                status = CustomEmailUiStatus.Unverified(
                    id = CustomEmailId("2")
                )
            ),
            CustomEmailUiState(
                email = "suggestion@email.address",
                status = CustomEmailUiStatus.Suggestion(
                    usedInLoginsCount = 3
                )
            )
        )
}

internal class ThemeCustomEmailItemPreviewProvider : ThemePairPreviewProvider<CustomEmailUiState>(
    CustomEmailItemPreviewProvider()
)
