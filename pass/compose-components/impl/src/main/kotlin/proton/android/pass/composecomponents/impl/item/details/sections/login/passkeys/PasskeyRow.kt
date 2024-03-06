/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainerStrong
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import me.proton.core.presentation.R as CoreR

@Composable
fun PasskeyRow(
    modifier: Modifier = Modifier,
    domain: String,
    username: String,
    onClick: () -> Unit
) {
    val labelTitle = stringResource(id = R.string.passkey_field_label)
    val label = remember {
        "$labelTitle • $domain"
    }

    Row(
        modifier = modifier
            .roundedContainerStrong()
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_passkey),
            contentDescription = null,
            tint = PassTheme.colors.loginInteractionNormMajor2
        )

        Column(modifier = Modifier.weight(1f)) {
            ProtonTextFieldLabel(
                text = label,
                color = ProtonTheme.colors.textWeak
            )

            Text(
                text = username,
                style = ProtonTheme.typography.defaultWeak()
            )
        }

        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_chevron_right),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@Preview
@Composable
fun PasskeyRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasskeyRow(
                domain = "test.domain",
                username = "test.username",
                onClick = {}
            )
        }
    }
}
