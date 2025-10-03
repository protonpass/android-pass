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

package proton.android.pass.features.security.center.customemail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.commonui.api.heroWeak
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.customemail.presentation.SecurityCenterCustomEmailState

@Composable
internal fun SecurityCenterCustomEmailContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterCustomEmailState,
    emailAddress: String,
    onUiEvent: (SecurityCenterCustomEmailUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                title = stringResource(R.string.security_center_custom_email_top_bar_title),
                onUpClick = { onUiEvent(SecurityCenterCustomEmailUiEvent.Back) }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(paddingValues = innerPaddingValues)
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.large)
        ) {
            ProtonTextField(
                value = emailAddress,
                onChange = { onUiEvent(SecurityCenterCustomEmailUiEvent.OnEmailChange(it)) },
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.security_center_custom_email_email_address_placeholder),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                isError = state.emailNotValid,
                errorMessage = stringResource(R.string.security_center_custom_email_invalid_address),
                textStyle = PassTheme.typography.heroNorm(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email
                ),
                onDoneClick = { onUiEvent(SecurityCenterCustomEmailUiEvent.AddCustomEmail) }
            )
            PassCircleButton(
                isLoading = state.isLoading,
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = stringResource(R.string.security_center_custom_email_continue),
                textColor = PassTheme.colors.interactionNormMajor2,
                onClick = { onUiEvent(SecurityCenterCustomEmailUiEvent.AddCustomEmail) }
            )
        }
    }
}
