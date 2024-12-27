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

package proton.android.pass.features.itemcreate.alias.mailboxes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectMailboxesMailboxRow(
    modifier: Modifier = Modifier,
    item: SelectedAliasMailboxUiModel,
    color: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onToggle() }
            .padding(Spacing.medium, Spacing.mediumSmall)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
                colors = CheckboxDefaults.colors(
                    checkedColor = color
                ),
                checked = item.selected,
                onCheckedChange = {
                    onToggle()
                }
            )
        }

        Spacer(modifier = Modifier.width(ProtonDimens.DefaultSpacing))
        Text(
            modifier = Modifier.weight(1.0f),
            text = item.model.email,
            style = ProtonTheme.typography.defaultNorm,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun SelectMailboxesMailboxRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectMailboxesMailboxRow(
                item = SelectedAliasMailboxUiModel(
                    model = AliasMailboxUiModel(id = 1, email = "some.test@email.local"),
                    selected = input.second
                ),
                color = PassTheme.colors.loginInteractionNorm,
                onToggle = {}
            )
        }
    }
}
