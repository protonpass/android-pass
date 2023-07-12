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

package proton.android.pass.featureprofile.impl.pinconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.featureprofile.impl.ProfileNavigation
import proton.android.pass.featureprofile.impl.R
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigValidationErrors.PinBlank
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigValidationErrors.PinDoesNotMatch

@Composable
fun PinConfigContent(
    modifier: Modifier = Modifier,
    state: PinConfigUiState,
    onNavigateEvent: (ProfileNavigation) -> Unit,
    onPinChange: (String) -> Unit,
    onRepeatPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                modifier = modifier,
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {},
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = Modifier.padding(12.dp, 4.dp),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { onNavigateEvent(ProfileNavigation.Back) }
                    )
                },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(16.dp, 0.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        isLoading = state.isLoading.value(),
                        text = {
                            Text(
                                text = stringResource(R.string.configure_pin_continue),
                                style = ProtonTheme.typography.defaultSmallNorm,
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = onSubmit
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.configure_pin_set_pin_code),
                style = PassTypography.hero
            )
            Text(
                text = stringResource(R.string.configure_pin_unlock_the_app_with_this_code),
                style = PassTypography.body3RegularWeak
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProtonTextField(
                value = state.pin,
                textStyle = PassTypography.hero,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next
                ),
                isError = state.validationErrors.contains(PinBlank),
                errorMessage = stringResource(R.string.configure_pin_pin_cannot_be_blank),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_pin_enter_pin_code),
                        textStyle = PassTypography.heroWeak
                    )
                },
                moveToNextOnEnter = true,
                onChange = onPinChange
            )
            ProtonTextField(
                value = state.repeatPin,
                textStyle = PassTypography.hero,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                isError = state.validationErrors.contains(PinDoesNotMatch),
                errorMessage = stringResource(R.string.configure_pin_pin_does_not_match),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_pin_repeat_pin_code),
                        textStyle = PassTypography.heroWeak
                    )
                },
                onChange = onRepeatPinChange
            )
        }
    }
}
