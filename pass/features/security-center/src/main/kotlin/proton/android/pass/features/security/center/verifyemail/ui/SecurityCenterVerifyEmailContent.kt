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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.codes.PassCodeVerificationContent
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.verifyemail.presentation.SecurityCenterVerifyEmailState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecurityCenterVerifyEmailContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterVerifyEmailState,
    code: String,
    onUiEvent: (SecurityCenterVerifyEmailUiEvent) -> Unit
) = with(state) {
    PassCodeVerificationContent(
        modifier = modifier,
        topBarTitle = stringResource(R.string.security_center_verify_email_title),
        topBarSubtitle = stringResource(
            R.string.security_center_verify_email_subtitle,
            email
        ),
        emailSubject = stringResource(CompR.string.verification_code_suggestion_subject_new_email_address),
        onUpClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.Back) },
        isActionLoading = isLoading,
        onActionClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.Verify) },
        verificationCode = code,
        onVerificationCodeChange = { onUiEvent(SecurityCenterVerifyEmailUiEvent.OnCodeChange(it)) },
        onResendVerificationCodeClick = { onUiEvent(SecurityCenterVerifyEmailUiEvent.ResendCode) },
        canEnterVerificationCode = !isLoading
    )
}
