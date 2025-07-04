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

package proton.android.pass.composecomponents.impl.item.details.sections.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun PassAliasSenderNameSection(
    modifier: Modifier = Modifier,
    text: String,
    title: String = stringResource(R.string.field_detail_display_name_title)
) {
    if (text.isNotBlank()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            RoundedCornersColumn {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
                ) {
                    Icon.Default(
                        id = CoreR.drawable.ic_proton_card_identity,
                        tint = PassTheme.colors.aliasInteractionNorm
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(space = Spacing.small)) {
                        SectionTitle(text = title)

                        SelectionContainer {
                            SectionSubtitle(
                                modifier = Modifier.fillMaxWidth(),
                                text = text.asAnnotatedString()
                            )
                        }
                    }
                }
            }
            Text.CaptionWeak(text = stringResource(R.string.display_name_section_description))
        }
    }
}

@[Preview Composable]
internal fun SenderNameSectionPreview(@PreviewParameter(ThemePreviewProvider::class) input: Boolean) {
    PassTheme(isDark = input) {
        Surface {
            PassAliasSenderNameSection(
                text = "John Doe"
            )
        }
    }
}
