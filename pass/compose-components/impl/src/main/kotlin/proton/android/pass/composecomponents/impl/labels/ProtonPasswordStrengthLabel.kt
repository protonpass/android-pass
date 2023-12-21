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

package proton.android.pass.composecomponents.impl.labels

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R

@Composable
fun ProtonPasswordStrengthLabel(
    passwordStrength: PasswordStrength,
    modifier: Modifier = Modifier,
    labelPrefix: String? = null,
) {
    val (labelResId, labelColor) = when (passwordStrength) {
        PasswordStrength.None -> return

        PasswordStrength.Strong -> Pair(
            R.string.label_password_strength_strong,
            PassTheme.colors.signalSuccess,
        )

        PasswordStrength.Vulnerable -> Pair(
            R.string.label_password_strength_vulnerable,
            PassTheme.colors.signalDanger,
        )

        PasswordStrength.Weak -> Pair(
            R.string.label_password_strength_weak,
            PassTheme.colors.signalWarning,
        )
    }

    val text = labelPrefix?.let { prefix -> "$prefix • ${stringResource(id = labelResId)}" }
        ?: stringResource(id = labelResId)

    Text(
        modifier = modifier,
        text = text,
        color = labelColor,
        style = PassTheme.typography.body3Norm(),
    )
}
