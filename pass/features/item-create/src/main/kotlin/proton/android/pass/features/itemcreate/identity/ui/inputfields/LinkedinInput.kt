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

package proton.android.pass.features.itemcreate.identity.ui.inputfields

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.itemcreate.R

@Composable
internal fun LinkedinInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    requestFocus: Boolean = false,
    onChange: (String) -> Unit,
    onClearFocus: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    ProtonTextField(
        modifier = modifier
            .padding(
                start = Spacing.medium,
                top = Spacing.medium,
                end = Spacing.small,
                bottom = Spacing.medium
            )
            .focusRequester(focusRequester),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        editable = enabled,
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.identity_field_linkedin_title)) },
        placeholder = {
            ProtonTextFieldPlaceHolder(
                text =
                stringResource(id = R.string.identity_field_linkedin_hint)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        }
    )
    RequestFocusLaunchedEffect(focusRequester, requestFocus, onClearFocus)
}
