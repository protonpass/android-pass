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

package proton.android.pass.composecomponents.impl.codes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.PassVerificationCodeTextField
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar

private const val VERIFICATION_CODE_LENGTH = 6

@Composable
fun PassCodeVerificationContent(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    topBarSubtitle: String,
    onUpClick: () -> Unit,
    isActionLoading: Boolean,
    onActionClick: () -> Unit,
    verificationCode: String,
    canEnterVerificationCode: Boolean,
    onVerificationCodeChange: (String) -> Unit,
    onResendVerificationCodeClick: () -> Unit,
    emailSubject: String,
    verificationCodeLength: Int = VERIFICATION_CODE_LENGTH
) {
    val isActionEnabled = remember(verificationCode) {
        verificationCode.length == verificationCodeLength
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.Cross,
                title = topBarTitle,
                subtitle = topBarSubtitle,
                onUpClick = onUpClick,
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = isActionLoading,
                        buttonEnabled = isActionEnabled,
                        color = if (isActionEnabled) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor1
                        },
                        text = {
                            Text.Body2Regular(
                                text = stringResource(id = R.string.action_continue),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = onActionClick
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = innerPaddingValues)
                .padding(
                    horizontal = Spacing.medium,
                    vertical = Spacing.large
                ),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PassVerificationCodeTextField(
                verificationCode = verificationCode,
                verificationCodeLength = verificationCodeLength,
                canEnterVerificationCode = canEnterVerificationCode,
                onVerificationCodeChange = onVerificationCodeChange
            )

            PassRequestVerificationCode(
                emailSubject = emailSubject,
                showRequestVerificationCodeOptions = canEnterVerificationCode,
                onResendVerificationCodeClick = onResendVerificationCodeClick
            )
        }
    }
}

@[Preview Composable]
internal fun PassCodeVerificationContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassCodeVerificationContent(
                topBarTitle = "Title",
                topBarSubtitle = "This is a subtitle",
                onUpClick = {},
                isActionLoading = false,
                onActionClick = {},
                verificationCode = "123456",
                canEnterVerificationCode = true,
                onVerificationCodeChange = {},
                onResendVerificationCodeClick = {},
                emailSubject = ""
            )
        }
    }
}
