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

package proton.android.pass.features.itemdetail.login.customfield

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.SectionSubtitle
import proton.android.pass.features.itemdetail.login.CustomFieldUiContent
import me.proton.core.presentation.R as CoreR

@Composable
fun CustomFieldText(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Text,
    onCopyValue: (String) -> Unit
) {
    RoundedCornersColumn(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCopyValue(entry.content) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_text_align_left),
                contentDescription = stringResource(R.string.custom_field_text_icon_description),
                tint = PassTheme.colors.loginInteractionNorm
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(text = entry.label)
                SelectionContainer {
                    SectionSubtitle(text = entry.content.asAnnotatedString())
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomFieldTextPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CustomFieldText(
                entry = CustomFieldUiContent.Text("label", "some value"),
                onCopyValue = {}
            )
        }
    }
}
