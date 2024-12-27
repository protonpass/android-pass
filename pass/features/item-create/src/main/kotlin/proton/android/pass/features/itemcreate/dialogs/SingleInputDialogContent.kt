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

package proton.android.pass.features.itemcreate.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder

@Composable
fun SingleInputDialogContent(
    modifier: Modifier = Modifier,
    canConfirm: Boolean,
    value: String,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int? = null,
    @StringRes placeholderRes: Int,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(vertical = 16.dp),
                title = stringResource(titleRes)
            )

            subtitleRes?.let {
                Text(
                    text = stringResource(subtitleRes),
                    style = ProtonTheme.typography.defaultWeak
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .roundedContainerNorm()
                    .padding(16.dp)
            ) {
                ProtonTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = value,
                    onChange = onChange,
                    placeholder = {
                        ProtonTextFieldPlaceHolder(
                            text = stringResource(placeholderRes)
                        )
                    },
                    textStyle = ProtonTheme.typography.defaultNorm,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onDoneClick = {
                        keyboardController?.hide()
                        onConfirm()
                    }
                )
            }
        }

        DialogCancelConfirmSection(
            modifier = Modifier.padding(16.dp),
            color = PassTheme.colors.loginInteractionNormMajor1,
            disabledColor = ProtonTheme.colors.interactionDisabled,
            confirmEnabled = canConfirm,
            onDismiss = onCancel,
            onConfirm = onConfirm
        )
    }

    RequestFocusLaunchedEffect(focusRequester)
}
