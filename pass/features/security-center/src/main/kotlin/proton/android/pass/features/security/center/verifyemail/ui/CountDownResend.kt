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

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.features.security.center.R
import kotlin.time.Duration.Companion.seconds

private const val COUNT_DOWN_SECONDS = 30
private const val COUNT_DOWN_SECONDS_FORMAT = "%02d"

@Composable
internal fun CountDownResend(modifier: Modifier = Modifier, onUiEvent: (SecurityCenterVerifyEmailUiEvent) -> Unit) {
    var countDownFinished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(COUNT_DOWN_SECONDS) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1.seconds)
            timeLeft--
        } else {
            countDownFinished = true
        }
    }

    if (countDownFinished) {
        TransparentTextButton(
            modifier = modifier,
            text = stringResource(R.string.security_center_verify_resend_code),
            color = PassTheme.colors.interactionNormMajor2,
            style = ProtonTheme.typography.defaultNorm,
            onClick = {
                countDownFinished = false
                timeLeft = COUNT_DOWN_SECONDS
                onUiEvent(SecurityCenterVerifyEmailUiEvent.ResendCode)
            }
        )
    } else {
        Text(
            text = stringResource(
                R.string.security_center_verify_resend_code_in_00,
                String.format(COUNT_DOWN_SECONDS_FORMAT, timeLeft)
            ),
            style = ProtonTheme.typography.defaultWeak
        )
    }
}
