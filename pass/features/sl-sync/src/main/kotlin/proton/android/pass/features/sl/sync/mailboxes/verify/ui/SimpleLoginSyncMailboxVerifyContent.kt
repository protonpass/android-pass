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

package proton.android.pass.features.sl.sync.mailboxes.verify.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.codes.PassCodeVerificationContent
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.verify.presentation.SimpleLoginSyncMailboxVerifyState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncMailboxVerifyContent(
    modifier: Modifier = Modifier,
    verificationCode: String,
    state: SimpleLoginSyncMailboxVerifyState,
    onUiEvent: (SimpleLoginSyncMailboxVerifyUiEvent) -> Unit
) = with(state) {
    PassCodeVerificationContent(
        modifier = modifier,
        topBarTitle = stringResource(id = R.string.simple_login_sync_mailbox_verify_title),
        topBarSubtitle = stringResource(
            id = R.string.simple_login_sync_mailbox_verify_subtitle,
            mailboxEmail.ifEmpty {
                stringResource(id = R.string.simple_login_sync_mailbox_verify_email_fallback)
            }
        ),
        emailSubject = stringResource(CompR.string.verification_code_suggestion_subject_new_mailbox),
        onUpClick = { onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnCloseClicked) },
        isActionLoading = isLoading,
        onActionClick = {
            onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnVerifyClicked)
        },
        verificationCode = verificationCode,
        canEnterVerificationCode = canEnterVerificationCode,
        onVerificationCodeChange = { newVerificationCode ->
            SimpleLoginSyncMailboxVerifyUiEvent.OnVerificationCodeChanged(
                newVerificationCode = newVerificationCode
            ).also(onUiEvent)
        },
        onResendVerificationCodeClick = {
            onUiEvent(SimpleLoginSyncMailboxVerifyUiEvent.OnResendVerificationCodeClicked)
        }
    )
}
