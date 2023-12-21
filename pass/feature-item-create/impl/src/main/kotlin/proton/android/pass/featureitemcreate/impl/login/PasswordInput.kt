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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState

@Composable
internal fun PasswordInput(
    value: UIHiddenState,
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(id = R.string.field_password_hint),
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onFocus: (Boolean) -> Unit,
) {
    val (text, visualTransformation) = when (value) {
        is UIHiddenState.Concealed -> "x".repeat(PASSWORD_CONCEALED_LENGTH) to PasswordVisualTransformation()
        is UIHiddenState.Revealed -> value.clearText to VisualTransformation.None
        is UIHiddenState.Empty -> "" to VisualTransformation.None
    }

    ProtonTextField(
        modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = text,
        editable = isEditAllowed,
        moveToNextOnEnter = true,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Password
        ),
        textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
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
private fun PasswordInputLabel(
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val (labelStrengthResId, labelColor) = when (passwordStrength) {
        PasswordStrength.None -> Pair(
            null,
            PassTheme.colors.textWeak,
        )

        PasswordStrength.Strong -> Pair(
            R.string.field_password_label_strength_strong,
            PassTheme.colors.signalSuccess,
        )

        PasswordStrength.Vulnerable -> Pair(
            R.string.field_password_label_strength_vulnerable,
            PassTheme.colors.signalDanger,
        )

        PasswordStrength.Weak -> Pair(
            R.string.field_password_label_strength_weak,
            PassTheme.colors.signalWarning,
        )
    }

    val passwordLabel = stringResource(id = R.string.field_password_title)
    val passwordStrengthLabel = labelStrengthResId
        ?.let { " \u2022 ${stringResource(id = it)}" }
        .orEmpty()

    Text(
        modifier = modifier,
        text = "$passwordLabel$passwordStrengthLabel",
        color = labelColor,
        style = PassTheme.typography.body3Norm(),
    )
}

@Composable
private fun PasswordInputLeadingIcon(
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val (iconResId, iconTint) = when (passwordStrength) {
        PasswordStrength.None -> Pair(
            me.proton.core.presentation.R.drawable.ic_proton_key,
            ProtonTheme.colors.iconWeak,
        )

        PasswordStrength.Strong -> Pair(
            R.drawable.ic_shield_success,
            PassTheme.colors.signalSuccess,
        )

        PasswordStrength.Vulnerable -> Pair(
            R.drawable.ic_shield_danger,
            PassTheme.colors.signalDanger,
        )

        PasswordStrength.Weak -> Pair(
            R.drawable.ic_shield_warning,
            PassTheme.colors.signalWarning,
        )
    }

    Icon(
        modifier = modifier,
        painter = painterResource(iconResId),
        tint = iconTint,
        contentDescription = null,
    )
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
                onFocus = {},
            )
        }
    }
}
