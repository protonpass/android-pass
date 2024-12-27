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

package proton.android.pass.features.itemcreate.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.icon.PassPasswordStrengthIcon
import proton.android.pass.composecomponents.impl.labels.PassPasswordStrengthLabel
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.UIHiddenState

@Composable
internal fun PasswordInput(
    value: UIHiddenState,
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(id = R.string.field_password_hint),
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onFocus: (Boolean) -> Unit
) {
    val (text, visualTransformation) = when (value) {
        is UIHiddenState.Concealed -> "x".repeat(PASSWORD_CONCEALED_LENGTH) to PasswordVisualTransformation()
        is UIHiddenState.Revealed -> value.clearText to VisualTransformation.None
        is UIHiddenState.Empty -> "" to VisualTransformation.None
    }

    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.none,
            top = Spacing.medium,
            end = Spacing.extraSmall,
            bottom = Spacing.medium
        ),
        value = text,
        editable = isEditAllowed,
        moveToNextOnEnter = true,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Password
        ),
        textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed)
            .copy(fontFamily = FontFamily.Monospace),
        onChange = onChange,
        label = { PasswordInputLabel(passwordStrength) },
        placeholder = { ProtonTextFieldPlaceHolder(text = placeholder) },
        leadingIcon = { PasswordInputLeadingIcon(passwordStrength) },
        trailingIcon = if (value is UIHiddenState.Revealed && text.isNotEmpty()) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        onFocusChange = { onFocus(it) }
    )
}

@Composable
private fun PasswordInputLabel(passwordStrength: PasswordStrength, modifier: Modifier = Modifier) {
    val text = stringResource(id = R.string.field_password_title)

    when (passwordStrength) {
        PasswordStrength.None -> ProtonTextFieldLabel(
            text = text,
            modifier = modifier
        )

        PasswordStrength.Strong,
        PasswordStrength.Vulnerable,
        PasswordStrength.Weak -> PassPasswordStrengthLabel(
            passwordStrength = passwordStrength,
            modifier = modifier,
            labelPrefix = text
        )
    }
}

@Composable
private fun PasswordInputLeadingIcon(passwordStrength: PasswordStrength, modifier: Modifier = Modifier) {
    when (passwordStrength) {
        PasswordStrength.None -> Icon(
            modifier = modifier,
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_key),
            tint = ProtonTheme.colors.iconWeak,
            contentDescription = null
        )

        PasswordStrength.Strong,
        PasswordStrength.Vulnerable,
        PasswordStrength.Weak -> PassPasswordStrengthIcon(
            passwordStrength = passwordStrength,
            modifier = modifier
        )
    }
}

class ThemePasswordInputPreviewProvider :
    ThemePairPreviewProvider<PasswordInputPreviewParams>(PasswordInputPreviewProvider())

@Preview
@Composable
fun PasswordInputPreview(
    @PreviewParameter(ThemePasswordInputPreviewProvider::class) input: Pair<Boolean, PasswordInputPreviewParams>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PasswordInput(
                value = input.second.hiddenState,
                passwordStrength = input.second.passwordStrength,
                isEditAllowed = input.second.isEditAllowed,
                onChange = {},
                onFocus = {}
            )
        }
    }
}
