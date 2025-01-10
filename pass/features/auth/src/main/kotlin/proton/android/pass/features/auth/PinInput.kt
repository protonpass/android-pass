/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonSelectableTextField

@Composable
fun PinInput(
    modifier: Modifier = Modifier,
    state: EnterPinUiState,
    onPinChanged: (String) -> Unit,
    onPinSubmit: () -> Unit
) {
    val data = state as? EnterPinUiState.Data
    val error = data?.pinError?.value()
    val focusRequester = remember { FocusRequester() }
    var hasFocus by rememberSaveable { mutableStateOf(false) }

    ProtonSelectableTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState -> hasFocus = focusState.hasFocus }
            .roundedContainer(
                backgroundColor = Color.Transparent,
                borderColor = PassTheme.colors.inputBorderNorm
            )
            .padding(Spacing.medium),
        text = data?.pin.orEmpty(),
        onTextChanged = onPinChanged,
        isEnabled = data?.isLoadingState?.value() != true,
        textStyle = PassTheme.typography.heroNorm().copy(textAlign = TextAlign.Center),
        keyboardActions = KeyboardActions(
            onDone = { onPinSubmit() }
        ),
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        visualTransformation = PasswordVisualTransformation(),
        errorText = stringResource(R.string.auth_error_pin_cannot_be_empty).takeIf { error is PinError.PinEmpty }
    )

    RequestFocusLaunchedEffect(
        focusRequester = focusRequester,
        requestFocus = !hasFocus
    )
}
