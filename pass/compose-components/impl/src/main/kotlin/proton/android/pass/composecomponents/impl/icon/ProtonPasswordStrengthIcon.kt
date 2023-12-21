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

package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun ProtonPasswordStrengthIcon(
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val (iconResId, iconTint) = when (passwordStrength) {
        PasswordStrength.None -> return

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

@Preview
@Composable
fun ProtonPasswordStrengthIconPreview(
    @PreviewParameter(ThemeProtonPasswordStrengthIconPreview::class) input: Pair<Boolean, PasswordStrength>,
) {
    val (isDark, passwordStrength) = input

    PassTheme(isDark = isDark) {
        Surface {
            ProtonPasswordStrengthIcon(passwordStrength = passwordStrength)
        }
    }
}

class ThemeProtonPasswordStrengthIconPreview :
    ThemePairPreviewProvider<PasswordStrength>(ProtonPasswordStrengthIconPreviewProvider())

private class ProtonPasswordStrengthIconPreviewProvider :
    PreviewParameterProvider<PasswordStrength> {

    override val values: Sequence<PasswordStrength> = PasswordStrength.values().asSequence()

}


