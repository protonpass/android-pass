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

package proton.android.pass.featurehome.impl.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.items.ItemSharedType
import proton.android.pass.featurehome.impl.R

@Composable
internal fun EmptySharedItems(modifier: Modifier = Modifier, itemSharedType: ItemSharedType) {
    val subtitleResId = remember(itemSharedType) {
        when (itemSharedType) {
            ItemSharedType.All -> R.string.home_empty_shared_items_title
            ItemSharedType.SharedByMe -> R.string.home_empty_shared_items_by_me_subtitle
            ItemSharedType.SharedWithMe -> R.string.home_empty_shared_items_with_me_subtitle
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Text.Headline(
                text = stringResource(id = R.string.home_empty_shared_items_title)
            )

            Text.Body3Regular(
                text = stringResource(id = subtitleResId),
                color = ProtonTheme.colors.textWeak
            )
        }
    }
}
