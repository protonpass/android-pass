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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents

@Composable
internal fun ItemRowContents(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String,
    vaultIcon: Int? = null,
    canLoadExternalImages: Boolean,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode,
    titleSuffix: Option<String>
) {
    when (item.contents) {
        is ItemContents.Login -> LoginRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            canLoadExternalImages = canLoadExternalImages,
            selection = selection
        )

        is ItemContents.Note -> NoteRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            selection = selection
        )

        is ItemContents.Alias -> AliasRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            selection = selection
        )

        is ItemContents.CreditCard -> CreditCardRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            selection = selection
        )

        is ItemContents.Identity -> IdentityRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            selection = selection
        )

        is ItemContents.Custom -> CustomRow(
            modifier = modifier,
            item = item,
            titleSuffix = titleSuffix,
            highlight = highlight,
            vaultIcon = vaultIcon,
            selection = selection
        )

        is ItemContents.Unknown -> {
        }

    }
}
