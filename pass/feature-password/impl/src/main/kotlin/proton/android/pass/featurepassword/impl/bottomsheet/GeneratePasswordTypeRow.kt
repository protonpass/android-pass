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

package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featurepassword.R
import proton.android.pass.featurepassword.impl.extensions.toResourceString
import proton.android.pass.preferences.PasswordGenerationMode
import me.proton.core.presentation.R as CoreR

@Composable
fun GeneratePasswordTypeRow(
    modifier: Modifier = Modifier,
    current: PasswordGenerationMode,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.password_type),
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm,
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = current.toResourceString(),
                color = PassTheme.colors.textNorm,
                style = ProtonTheme.typography.defaultSmallNorm,
            )

            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_chevron_down_filled),
                contentDescription = stringResource(R.string.password_mode_icon),
                tint = PassTheme.colors.textHint
            )
        }
    }
}

@Preview
@Composable
fun GeneratePasswordTypeRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val current = if (input.second) PasswordGenerationMode.Random else PasswordGenerationMode.Words
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordTypeRow(
                current = current,
                onClick = {}
            )
        }
    }
}
