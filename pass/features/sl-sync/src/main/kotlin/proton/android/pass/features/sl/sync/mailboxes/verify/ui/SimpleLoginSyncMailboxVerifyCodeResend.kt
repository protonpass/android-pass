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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.utils.passTimerText
import proton.android.pass.features.sl.sync.R

@Composable
internal fun SimpleLoginSyncMailboxVerifyCodeResend(
    modifier: Modifier = Modifier,
    canRequestVerificationCode: Boolean,
    verificationCodeTimerSeconds: Int
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = canRequestVerificationCode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(
                    id = R.string.simple_login_sync_mailbox_verify_code_not_received,
                    passTimerText(seconds = verificationCodeTimerSeconds)
                ),
                color = PassTheme.colors.textWeak,
                style = ProtonTheme.typography.body1Regular
            )
        }

        AnimatedVisibility(
            visible = !canRequestVerificationCode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(
                    id = R.string.simple_login_sync_mailbox_verify_code_resend,
                    passTimerText(seconds = verificationCodeTimerSeconds)
                ),
                color = PassTheme.colors.textWeak,
                style = ProtonTheme.typography.body1Regular
            )
        }
    }
}
