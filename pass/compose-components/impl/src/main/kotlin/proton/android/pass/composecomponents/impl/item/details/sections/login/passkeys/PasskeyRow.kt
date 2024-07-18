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

package proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.items.ItemCategory

@Composable
internal fun PasskeyRow(
    modifier: Modifier = Modifier,
    domain: String,
    username: String,
    itemDiffType: ItemDiffType,
    itemColors: PassItemColors,
    onClick: () -> Unit
) {
    val titleLabel = stringResource(id = R.string.passkey_field_label)
    val title = remember {
        "$titleLabel • $domain"
    }
    RoundedCornersColumn(
        modifier = modifier,
        backgroundColor = itemColors.minorSecondary
    ) {
        PassItemDetailFieldRow(
            icon = R.drawable.ic_person_key,
            title = title,
            subtitle = username,
            itemColors = itemColors,
            itemDiffType = itemDiffType,
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_tiny_right),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
    }
}

@Preview
@Composable
internal fun PasskeyRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasskeyRow(
                domain = "test.domain",
                username = "test.username",
                itemDiffType = ItemDiffType.None,
                itemColors = passItemColors(itemCategory = ItemCategory.Login),
                onClick = {}
            )
        }
    }
}
