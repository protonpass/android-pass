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

package proton.android.pass.features.extrapassword.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.features.extrapassword.R
import proton.android.pass.features.extrapassword.presentation.ExtraPasswordError
import proton.android.pass.features.extrapassword.presentation.ExtraPasswordState

@Composable
internal fun EnterAccessKeyContent(
    modifier: Modifier = Modifier,
    state: ExtraPasswordState,
    content: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val (visualTransformation, actionIcon, actionContent) = if (passwordVisible) {
        EnterExtraPasswordUiContent(
            visualTransformation = VisualTransformation.None,
            trailingIcon = me.proton.core.presentation.R.drawable.ic_proton_eye,
            trailingIconContentDescription = stringResource(R.string.extra_password_conceal_password_action)
        )
    } else {
        EnterExtraPasswordUiContent(
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = me.proton.core.presentation.R.drawable.ic_proton_eye_slash,
            trailingIconContentDescription = stringResource(R.string.extra_password_reveal_password_action)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxWidth()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.extra_password_enter_extra_password),
                style = ProtonTheme.typography.headlineNorm
            )

            ProtonTextField(
                modifier = Modifier
                    .roundedContainerNorm()
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.none,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = content,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                textStyle = ProtonTheme.typography.defaultNorm(),
                onChange = onValueChange,
                label = {
                    ProtonTextFieldLabel(
                        text = stringResource(R.string.extra_password_label),
                        isError = state.isError
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                },
                trailingIcon = {
                    CircleIconButton(
                        backgroundColor = Color.Unspecified,
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            painter = painterResource(actionIcon),
                            contentDescription = actionContent,
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                },
                isError = state.isError,
                errorMessage = when (state.error.value()) {
                    ExtraPasswordError.EmptyPassword -> stringResource(R.string.extra_password_error_empty)
                    ExtraPasswordError.WrongPassword -> stringResource(R.string.extra_password_error_wrong)
                    else -> ""
                },
                visualTransformation = visualTransformation,
                onDoneClick = onSubmit
            )


            LoadingCircleButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.medium),
                buttonHeight = 40.dp,
                text = {
                    Text(
                        text = stringResource(me.proton.core.presentation.compose.R.string.presentation_alert_submit),
                        color = PassTheme.colors.textInvert
                    )
                },
                color = PassTheme.colors.interactionNormMajor2,
                isLoading = state.loadingState.value(),
                onClick = onSubmit
            )
        }
    }
}

private data class EnterExtraPasswordUiContent(
    val visualTransformation: VisualTransformation,
    @DrawableRes val trailingIcon: Int,
    val trailingIconContentDescription: String
)
