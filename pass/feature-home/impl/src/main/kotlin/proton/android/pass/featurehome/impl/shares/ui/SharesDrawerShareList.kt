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

package proton.android.pass.featurehome.impl.shares.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featurehome.impl.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SharesDrawerShareList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            SharesDrawerShareRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = stringResource(id = R.string.vault_drawer_all_vaults),
                itemsCount = 4,
                membersCount = 0,
                isSelected = true,
                onClick = { }
            )
        }

        item {
            PassDivider(
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }

        item {
            SharesDrawerShareRow(
                shareIconRes = CoreR.drawable.ic_proton_trash,
                iconColor = PassTheme.colors.textWeak,
                iconBackgroundColor = PassTheme.colors.textDisabled,
                name = stringResource(id = R.string.vault_drawer_item_trash),
                itemsCount = 0,
                membersCount = 0,
                isSelected = false,
                onClick = { },
                onMenuOptionsClick = {}
            )
        }
    }
}
