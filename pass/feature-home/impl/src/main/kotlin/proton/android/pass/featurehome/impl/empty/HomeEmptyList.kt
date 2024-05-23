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

package proton.android.pass.featurehome.impl.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featurehome.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun HomeEmptyList(
    modifier: Modifier = Modifier,
    isIdentityEnabled: Boolean,
    onCreateLoginClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onCreateCreditCardClick: () -> Unit,
    onCreateIdentityClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeEmptyHeader()
            Spacer(modifier = Modifier.height(16.dp))
            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.home_empty_vault_create_login),
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                textColor = PassTheme.colors.loginInteractionNormMajor2,
                icon = CoreR.drawable.ic_proton_user,
                onClick = onCreateLoginClick
            )
            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.home_empty_vault_create_alias),
                backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                textColor = PassTheme.colors.aliasInteractionNormMajor2,
                icon = CoreR.drawable.ic_proton_alias,
                onClick = onCreateAliasClick
            )
            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.home_empty_vault_create_note),
                backgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                textColor = PassTheme.colors.noteInteractionNormMajor2,
                icon = CoreR.drawable.ic_proton_notepad_checklist,
                onClick = onCreateNoteClick
            )
            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.home_empty_vault_create_credit_card),
                backgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                textColor = PassTheme.colors.cardInteractionNormMajor2,
                icon = CoreR.drawable.ic_proton_credit_card,
                onClick = onCreateCreditCardClick
            )
            if (isIdentityEnabled) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_identity),
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    textColor = PassTheme.colors.interactionNormMajor2,
                    icon = CoreR.drawable.ic_proton_card_identity,
                    onClick = onCreateIdentityClick
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeEmptyListPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeEmptyList(
                isIdentityEnabled = true,
                onCreateLoginClick = {},
                onCreateAliasClick = {},
                onCreateNoteClick = {},
                onCreateCreditCardClick = {},
                onCreateIdentityClick = {}
            )
        }
    }
}
