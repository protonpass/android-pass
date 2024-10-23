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

package proton.android.pass.features.alias.contacts.create.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadlineUnspecified
import me.proton.core.compose.theme.textNorm
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.aliascontacts.R

@Composable
internal fun EmailInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    isError: Boolean,
    onChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    ProtonTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        isError = isError,
        textStyle = ProtonTheme.typography.subheadlineUnspecified.copy(
            color = ProtonTheme.colors.textNorm(enabled)
        ),
        editable = enabled,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email
        ),
        errorMessage = stringResource(id = R.string.email_input_error),
        trailingIcon = {
            if (enabled && value.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        }
    )
    RequestFocusLaunchedEffect(focusRequester, true) {}
}
