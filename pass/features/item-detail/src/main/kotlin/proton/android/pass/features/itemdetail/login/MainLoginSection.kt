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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.HiddenState
import proton.android.pass.features.itemdetail.login.totp.TotpRow

@Composable
internal fun MainLoginSection(
    modifier: Modifier = Modifier,
    email: String,
    username: String,
    passwordHiddenState: HiddenState,
    passwordScore: PasswordScore?,
    totpUiState: TotpUiState?,
    showViewAlias: Boolean,
    onEvent: (LoginDetailEvent) -> Unit
) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if (email.isNotBlank()) {
        sections.add {
            LoginEmailRow(
                email = email,
                showViewAlias = showViewAlias,
                onEmailClick = { onEvent(LoginDetailEvent.OnEmailClick(email)) },
                onGoToAliasClick = { onEvent(LoginDetailEvent.OnGoToAliasClick) }
            )
        }
    }

    if (username.isNotBlank()) {
        sections.add {
            LoginUsernameRow(
                username = username,
                onUsernameClick = { onEvent(LoginDetailEvent.OnUsernameClick) }
            )
        }
    }

    if (passwordHiddenState !is HiddenState.Empty) {
        sections.add {
            LoginPasswordRow(
                passwordHiddenState = passwordHiddenState,
                passwordScore = passwordScore,
                onTogglePasswordClick = {
                    onEvent(LoginDetailEvent.OnTogglePasswordClick)
                },
                onCopyPasswordClick = {
                    onEvent(LoginDetailEvent.OnCopyPasswordClick)
                }
            )
        }
    }

    totpUiState?.let { state ->
        sections.add {
            TotpRow(
                state = state,
                onCopyTotpClick = { totpCode ->
                    onEvent(LoginDetailEvent.OnCopyTotpClick(totpCode))
                },
                onUpgradeClick = {
                    onEvent(LoginDetailEvent.OnUpgradeClick)
                }
            )
        }
    }

    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        sections.forEachIndexed { idx, section ->
            section()

            if (idx < sections.lastIndex) {
                PassDivider()
            }
        }
    }
}

internal class ThemedLoginPasswordRowPreviewProvider :
    ThemePairPreviewProvider<MainLoginSectionParams>(MainLoginSectionParamsPreviewProvider())

@[Preview Composable]
internal fun MainLoginSectionPreview(
    @PreviewParameter(ThemedLoginPasswordRowPreviewProvider::class) input: Pair<Boolean, MainLoginSectionParams>
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            MainLoginSection(
                email = params.email,
                username = params.username,
                passwordHiddenState = params.passwordState,
                passwordScore = PasswordScore.STRONG,
                totpUiState = params.totpUiState,
                showViewAlias = params.showViewAlias,
                onEvent = {}
            )
        }
    }
}
