/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect

@Composable
fun EmailInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    requestFocus: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    ProtonTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = value,
        editable = enabled,
        isError = isError,
        errorMessage = errorMessage,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false
        ),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon ?: {
            if (enabled && value.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        },
        onFocusChange = onFocusChange,
        onChange = onChange
    )
    RequestFocusLaunchedEffect(focusRequester, requestFocus)
}
