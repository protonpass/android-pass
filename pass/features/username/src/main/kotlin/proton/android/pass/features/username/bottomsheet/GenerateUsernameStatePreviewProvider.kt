/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonrust.api.usernames.UsernameConfig

internal class GenerateUsernameStatePreviewProvider : PreviewParameterProvider<GenerateUsernameUiState> {

    override val values: Sequence<GenerateUsernameUiState> = sequenceOf(
        GenerateUsernameUiState(
            username = "happy-cat",
            config = UsernameConfig.Default,
            mode = GenerateUsernameMode.CopyAndClose,
            event = GenerateUsernameEvent.Idle
        ),
        GenerateUsernameUiState(
            username = "shiny-otter-runs",
            config = UsernameConfig.Default.copy(wordCount = 3),
            mode = GenerateUsernameMode.CancelConfirm,
            event = GenerateUsernameEvent.Idle
        )
    )
}
