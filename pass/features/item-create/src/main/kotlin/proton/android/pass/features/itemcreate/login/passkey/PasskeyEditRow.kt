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

package proton.android.pass.features.itemcreate.login.passkey

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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PasskeyEditRow(
    modifier: Modifier = Modifier,
    domain: String,
    username: String,
    canDelete: Boolean,
    onDeleteClick: () -> Unit
) {
    val labelTitle = stringResource(id = CompR.string.passkey_field_label)
    val label = remember {
        "$labelTitle ${SpecialCharacters.DOT_SEPARATOR} $domain"
    }

    Row(
        modifier = modifier
            .roundedContainerNorm()
            .fillMaxWidth()
            .padding(
                start = Spacing.medium,
                top = Spacing.medium,
                end = Spacing.extraSmall,
                bottom = Spacing.medium
            ),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_person_key),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )

        Column(modifier = Modifier.weight(1f)) {
            ProtonTextFieldLabel(
                text = label
            )

            Text(
                text = username,
                style = ProtonTheme.typography.defaultNorm(),
                color = PassTheme.colors.textWeak
            )
        }

        if (canDelete) {
            SmallCrossIconButton(onClick = onDeleteClick)
        }
    }
}

@Preview
@Composable
internal fun PasskeyEditRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, canDelete) = input

    PassTheme(isDark = isDark) {
        Surface {
            PasskeyEditRow(
                domain = "domain.local",
                username = "some@user.name",
                canDelete = canDelete,
                onDeleteClick = {}
            )
        }
    }
}
