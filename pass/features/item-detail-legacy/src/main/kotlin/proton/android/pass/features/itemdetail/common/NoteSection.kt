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

package proton.android.pass.features.itemdetail.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.itemdetail.NoteDetailSectionPreviewProvider
import proton.android.pass.features.itemdetail.R

@Composable
internal fun NoteSection(
    modifier: Modifier = Modifier,
    text: String,
    accentColor: Color,
    title: String = stringResource(R.string.field_detail_note_title)
) {
    RoundedCornersColumn(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_note),
                contentDescription = null,
                tint = accentColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(space = Spacing.small)) {
                SectionTitle(text = title)

                SelectionContainer {
                    SectionSubtitle(text = text.asAnnotatedString())
                }
            }
        }
    }
}

internal class ThemedDetailNoteSectionPreviewProvider :
    ThemePairPreviewProvider<String>(NoteDetailSectionPreviewProvider())

@[Preview Composable]
internal fun NoteSectionPreview(
    @PreviewParameter(ThemedDetailNoteSectionPreviewProvider::class) input: Pair<Boolean, String>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteSection(
                text = input.second,
                accentColor = PassTheme.colors.loginInteractionNormMajor1
            )
        }
    }
}
