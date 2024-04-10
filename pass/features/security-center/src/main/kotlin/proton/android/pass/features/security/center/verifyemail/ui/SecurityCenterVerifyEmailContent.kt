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

package proton.android.pass.features.security.center.verifyemail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.commonui.api.heroWeak
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.verifyemail.presentation.SecurityCenterVerifyEmailState

@Composable
internal fun SecurityCenterVerifyEmailContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterVerifyEmailState,
    code: String,
    onUiEvent: (SecurityCenterVerifyEmailUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                title = stringResource(R.string.security_center_verify_email_title),
                subtitle = stringResource(
                    R.string.security_center_verify_email_subtitle,
                    state.email
                ),
                onUpClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.Back) },
                actions = {
                    LoadingCircleButton(
                        isLoading = state.isLoadingState.value(),
                        color = PassTheme.colors.interactionNormMajor2,
                        text = {
                            Text(
                                text = stringResource(R.string.security_center_verify_email_continue),
                                style = ProtonTheme.typography.defaultSmallNorm
                            )
                        },
                        onClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.Verify) }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .padding(all = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            ProtonTextField(
                value = code,
                onChange = { onUiEvent(SecurityCenterVerifyEmailUiEvent.OnCodeChange(it)) },
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.security_center_verify_email_code_placeholder),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                isError = state.isError,
                errorMessage = stringResource(R.string.security_center_verify_email_code_error),
                textStyle = PassTheme.typography.heroNorm(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                onDoneClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.Verify) }
            )
        }
    }
}
