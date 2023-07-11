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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreenBody(
    modifier: Modifier = Modifier,
    state: AuthContent,
    onEvent: (AuthUiEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSubmit = {
        keyboardController?.hide()
        onEvent(AuthUiEvent.OnPasswordSubmit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        AuthScreenMasterPasswordForm(
            state = state,
            onEvent = onEvent,
            onSubmit = onSubmit
        )

        Spacer(modifier = Modifier.weight(1f))

        LoadingCircleButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = {
                Text(
                    text = stringResource(R.string.auth_unlock_button),
                    color = PassTheme.colors.textInvert
                )
            },
            color = PassTheme.colors.interactionNormMajor2,
            isLoading = state.isLoadingState.value(),
            onClick = onSubmit
        )
    }
}

