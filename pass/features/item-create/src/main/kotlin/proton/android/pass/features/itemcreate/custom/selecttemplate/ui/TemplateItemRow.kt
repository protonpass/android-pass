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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.itemcreate.custom.shared.TemplateType

@Composable
fun TemplateItem(
    modifier: Modifier = Modifier,
    item: TemplateType,
    onClick: (TemplateEvent) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(PassTheme.shapes.squircleMediumLargeShape)
            .clickable { onClick(TemplateEvent.OnTemplateSelected(item.some())) },
        shape = PassTheme.shapes.squircleMediumLargeShape
    ) {
        val (bgColor, fgColor) = when (item.category) {
            TemplateType.Category.TECHNOLOGY ->
                PassTheme.colors.inputBackgroundStrong to PassTheme.colors.interactionNormMajor2

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
internal fun TemplateItemPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TemplateItem(
                item = TemplateType.SERVER,
                onClick = {}
            )
        }
    }
}
