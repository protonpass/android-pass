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

package proton.android.pass.features.itemdetail.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.domain.HiddenState

@Suppress("MagicNumber")
internal class MainLoginSectionParamsPreviewProvider :
    PreviewParameterProvider<MainLoginSectionParams> {

    private val email = "user@email.com"
    private val username = "MyUsername"

    override val values: Sequence<MainLoginSectionParams> = sequenceOf(
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Concealed("encrypted"),
            totpUiState = null,
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Concealed("encrypted"),
            totpUiState = null,
            showViewAlias = true
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Revealed("encrypted", "clearText"),
            totpUiState = null,
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Revealed("encrypted", "clearText"),
            totpUiState = TotpUiState.Visible("123456", 12, 20),
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Revealed("encrypted", "clearText"),
            totpUiState = TotpUiState.Limited,
            showViewAlias = false
        ),

        // Hidden sections
        MainLoginSectionParams(
            email = "",
            username = "",
            passwordState = HiddenState.Concealed("encrypted"),
            totpUiState = TotpUiState.Visible("123456", 12, 20),
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Empty(""),
            totpUiState = TotpUiState.Visible("123456", 12, 20),
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Concealed("encrypted"),
            totpUiState = TotpUiState.Hidden,
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = "",
            username = "",
            passwordState = HiddenState.Empty(""),
            totpUiState = TotpUiState.Visible("123456", 12, 20),
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = "",
            username = "",
            passwordState = HiddenState.Concealed("encrypted"),
            totpUiState = TotpUiState.Hidden,
            showViewAlias = false
        ),
        MainLoginSectionParams(
            email = email,
            username = username,
            passwordState = HiddenState.Empty(""),
            totpUiState = TotpUiState.Hidden,
            showViewAlias = false
        )
    )
}

internal data class MainLoginSectionParams(
    val email: String,
    val username: String,
    val passwordState: HiddenState,
    val totpUiState: TotpUiState?,
    val showViewAlias: Boolean
)
