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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.login.SSN_CONCEALED_LENGTH

@Composable
internal fun SocialSecurityNumberInput(
    modifier: Modifier = Modifier,
    value: UIHiddenState,
    enabled: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val (text, visualTransformation) = remember(key1 = value) {
        when (value) {
            is UIHiddenState.Concealed -> "x".repeat(SSN_CONCEALED_LENGTH) to PasswordVisualTransformation()
            is UIHiddenState.Revealed -> value.clearText to VisualTransformation.None
            is UIHiddenState.Empty -> "" to VisualTransformation.None
        }
    }

    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.medium,
            top = Spacing.medium,
            end = Spacing.small,
            bottom = Spacing.medium
        ),
        value = text,
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text
        ),
        editable = enabled,
        label = {
            ProtonTextFieldLabel(
                text = stringResource(id = R.string.identity_field_social_security_number_title)
            )
        },
        placeholder = {
            ProtonTextFieldPlaceHolder(
                text = stringResource(id = R.string.identity_field_social_security_number_hint)
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                SmallCrossIconButton(enabled = true) { onChange("") }
            }
        },
        onFocusChange = onFocusChange
    )
}
