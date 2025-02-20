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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text

@Composable
fun TemplateItem(item: TemplateType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val (bgColor, fgColor) = when (item.category) {
            TemplateType.Category.TECHNOLOGY ->
                PassTheme.colors.backgroundWeak to PassTheme.colors.interactionNormMajor2
            TemplateType.Category.FINANCE ->
                PassTheme.colors.noteInteractionNormMinor2 to PassTheme.colors.noteInteractionNormMajor2
            TemplateType.Category.PERSONAL ->
                PassTheme.colors.aliasInteractionNormMinor2 to PassTheme.colors.aliasInteractionNormMajor2
        }
        Row(
            modifier = Modifier
                .background(bgColor)
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Icon.Default(id = item.iconResId, tint = fgColor)
            Text.Body1Regular(text = stringResource(item.titleResId))
        }
    }
}

@Preview
@Composable
fun TemplateItemPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TemplateItem(TemplateType.SERVER)
        }
    }
}
