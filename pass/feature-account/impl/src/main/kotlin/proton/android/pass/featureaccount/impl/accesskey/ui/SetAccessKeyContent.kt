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

package proton.android.pass.featureaccount.impl.accesskey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.commonui.api.heroWeak
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.featureaccount.impl.R
import proton.android.pass.featureaccount.impl.accesskey.navigation.SetAccessKeyContentEvent
import proton.android.pass.featureaccount.impl.accesskey.presentation.SetAccessKeyState

@Composable
fun SetAccessKeyContent(
    modifier: Modifier = Modifier,
    state: SetAccessKeyState,
    onEvent: (SetAccessKeyContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {},
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = Modifier.padding(12.dp, 4.dp),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { onEvent(SetAccessKeyContentEvent.Back) }
                    )
                },
                actions = {
                    CircleButton(
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = 0.dp),
                        contentPadding = PaddingValues(Spacing.medium, Spacing.small),
                        color = PassTheme.colors.interactionNormMajor1,
                        content = {
                            Text(
                                text = stringResource(R.string.access_key_continue),
                                style = ProtonTheme.typography.defaultSmallNorm,
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(SetAccessKeyContentEvent.Submit) }
                    )
                }
            )
        }
    ) { padding ->
        val focusRequester = remember { FocusRequester() }
        RequestFocusLaunchedEffect(focusRequester, true)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.configure_access_code),
                style = PassTheme.typography.heroNorm()
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = state.password,
                textStyle = PassTheme.typography.heroNorm(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next
                ),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_access_key_enter_access_key),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                moveToNextOnEnter = true,
                onChange = { onEvent(SetAccessKeyContentEvent.OnAccessKeyValueChanged(it)) }
            )
            ProtonTextField(
                value = state.repeatPassword,
                textStyle = PassTheme.typography.heroNorm(),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_access_key_repeat_access_key),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                onChange = { onEvent(SetAccessKeyContentEvent.OnAccessKeyRepeatValueChanged(it)) },
                onDoneClick = { onEvent(SetAccessKeyContentEvent.Submit) }
            )
        }
    }
}
