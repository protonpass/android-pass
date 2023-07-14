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

package proton.android.pass.featureauth.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun EnterPinContent(
    modifier: Modifier = Modifier,
    state: EnterPinUiState,
    onPinChanged: (String) -> Unit,
    onPinSubmit: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, CenterVertically)
    ) {
        Icon(
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp),
            painter = painterResource(proton.android.pass.composecomponents.impl.R.drawable.ic_pass_logo),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Text(
            text = stringResource(R.string.enter_your_pin_code),
            style = ProtonTheme.typography.headlineNorm
        )
        PinInput(
            state = state,
            onPinChanged = onPinChanged,
            onPinSubmit = onPinSubmit
        )
        val pinError = (state as? EnterPinUiState.Data)?.pinError?.value()
        if (pinError is PinError.PinIncorrect) {
            Text(
                text = (pinError as? PinError.PinIncorrect)?.let {
                    pluralStringResource(
                        id = R.plurals.enter_your_pin_code_error,
                        count = it.remainingAttempts,
                        it.remainingAttempts
                    )
                } ?: "",
                style = ProtonTheme.typography.defaultSmallNorm,
                color = PassTheme.colors.signalDanger
            )
        } else {
            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            )
        }

        LoadingCircleButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            buttonHeight = 40.dp,
            color = PassTheme.colors.interactionNormMajor1,
            onClick = onPinSubmit,
            isLoading = state is EnterPinUiState.Data && state.isLoadingState is IsLoadingState.Loading,
            text = {
                Text(
                    text = stringResource(R.string.unlock),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        )
    }
}


@Preview
@Composable
fun EnterPinContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            EnterPinContent(
                state = EnterPinUiState.NotInitialised,
                onPinChanged = {},
                onPinSubmit = {}
            )
        }
    }
}
