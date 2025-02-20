/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.template.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.itemcreate.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(Spacing.medium)) {
        item { }
        TemplateType.entries.groupBy { it.category }
            .forEach { (category, items) ->
                val header = when (category) {
                    TemplateType.Category.TECHNOLOGY -> R.string.template_header_technology
                    TemplateType.Category.FINANCE -> R.string.template_header_finance
                    TemplateType.Category.PERSONAL -> R.string.template_header_personal
                }
                stickyHeader {
                    Text.Body2Weak(
                        text = stringResource(header),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.small)
                    )
                }

                items(items) { item ->
                    TemplateItem(item = item)
                }
            }
    }
}

@Preview
@Composable
fun TemplateContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TemplateContent()
        }
    }
}
