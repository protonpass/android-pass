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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
internal fun SimpleLoginSyncMailboxVerifyCodeSection(
    modifier: Modifier = Modifier,
    verificationCode: String,
    verificationCodeLength: Int,
    verificationCodeTimerSeconds: Int,
    showResendVerificationCodeTimer: Boolean,
    canRequestVerificationCode: Boolean,
    canEnterVerificationCode: Boolean,
    onVerificationCodeChange: (String) -> Unit,
    onResendVerificationCodeClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.large)
    ) {
        SimpleLoginSyncMailboxVerifyCodeInput(
            verificationCode = verificationCode,
            verificationCodeLength = verificationCodeLength,
            onVerificationCodeChange = onVerificationCodeChange,
            canEnterVerificationCode = canEnterVerificationCode
        )

        SimpleLoginSyncMailboxVerifyCodeResend(
            verificationCodeTimerSeconds = verificationCodeTimerSeconds,
            showResendVerificationCodeTimer = showResendVerificationCodeTimer,
            canRequestVerificationCode = canRequestVerificationCode,
            onResendVerificationCodeClick = onResendVerificationCodeClick
        )
    }
}

@[Preview Composable]
internal fun SimpleLoginSyncMailboxVerifyCodePreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, showResendVerificationCodeTimer) = input

    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncMailboxVerifyCodeSection(
                verificationCode = "123",
                verificationCodeLength = 6,
                verificationCodeTimerSeconds = 30,
                showResendVerificationCodeTimer = showResendVerificationCodeTimer,
                canRequestVerificationCode = !showResendVerificationCodeTimer,
                canEnterVerificationCode = true,
                onVerificationCodeChange = {},
                onResendVerificationCodeClick = {}
            )
        }
    }
}
