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

package proton.android.pass.features.itemcreate.custom.selecttemplate.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.CrossTopAppBar
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.custom.shared.TemplateType

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TemplateContent(modifier: Modifier = Modifier, onEvent: (TemplateEvent) -> Unit) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            CrossTopAppBar(
                title = stringResource(R.string.select_template_title),
                onUpClick = { onEvent(TemplateEvent.OnBackClick) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Spacing.medium)
        ) {
            item {
                StartFromScratchButton(
                    modifier = Modifier.padding(bottom = Spacing.medium),
                    onClick = { onEvent(TemplateEvent.OnTemplateSelected(None)) }
                )
            }
            TemplateType.entries.groupBy { it.category }
                .forEach { (category, items) ->
                    val header = when (category) {
                        TemplateType.Category.TECHNOLOGY -> R.string.template_header_technology
                        TemplateType.Category.FINANCE -> R.string.template_header_finance
                        TemplateType.Category.PERSONAL -> R.string.template_header_title_personal
                    }

                    stickyHeader {
                        Text.Body2Weak(
                            text = stringResource(header),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PassTheme.colors.backgroundNorm)
                                .padding(vertical = Spacing.small)
                        )
                    }

                    items(items, key = TemplateType::id) { item ->
                        TemplateItem(
                            modifier = Modifier.padding(vertical = Spacing.extraSmall),
                            item = item,
                            onClick = onEvent
                        )
                    }
                }
        }
    }
}

@Preview
@Composable
internal fun TemplateContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TemplateContent(
                onEvent = {}
            )
        }
    }
}
