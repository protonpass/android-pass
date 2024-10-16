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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.aliascontacts.R

@Composable
fun EmptyContacts(modifier: Modifier = Modifier, onEvent: (DetailAliasContactUIEvent) -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Image.Default(R.drawable.empty_contacts)
        Text.Headline(stringResource(R.string.empty_alias_contacts_title))
        Text.Body2Regular(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.empty_alias_contacts_body),
            textAlign = TextAlign.Center
        )
        Button.Circular(
            color = PassTheme.colors.aliasInteractionNormMinor1,
            onClick = { onEvent(DetailAliasContactUIEvent.LearnMore) }
        ) {
            Text.Body3Regular(
                text = stringResource(R.string.empty_alias_contacts_learn_more),
                color = PassTheme.colors.aliasInteractionNormMajor2
            )
        }
    }
}

@Preview
@Composable
fun EmptyAliasContactsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            EmptyContacts(onEvent = {})
        }
    }
}
