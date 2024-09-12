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

package proton.android.pass.features.report.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.report.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ReportCategoriesPage(modifier: Modifier = Modifier, onReasonClicked: (ReportReason) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        Text.Headline(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            text = stringResource(R.string.categories_title)
        )
        Spacer(Modifier.height(Spacing.medium))
        Column(modifier = Modifier.padding(horizontal = Spacing.medium).roundedContainerNorm()) {
            ReportReason.entries.forEachIndexed { index, reportReason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReasonClicked(reportReason) }
                        .padding(horizontal = Spacing.medium, vertical = Spacing.medium + Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text.Body1Regular(reportReason.name)
                    Icon.Default(CompR.drawable.ic_chevron_tiny_right, tint = PassTheme.colors.textHint)
                }
                if (index != ReportReason.entries.lastIndex) {
                    PassDivider()
                }
            }
        }
    }
}

@Preview
@Composable
fun ReportCategoriesPagePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ReportCategoriesPage(
                onReasonClicked = { }
            )
        }
    }
}
