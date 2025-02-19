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

package proton.android.pass.features.secure.links.overview.ui.shared.headers

import androidx.compose.runtime.Composable
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.item.CreditCardRow
import proton.android.pass.composecomponents.impl.item.CustomRow
import proton.android.pass.composecomponents.impl.item.IdentityRow
import proton.android.pass.composecomponents.impl.item.LoginRow
import proton.android.pass.composecomponents.impl.item.NoteRow
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.items.ItemCategory

@Composable
internal fun SecureLinksOverviewHeader(
    item: ItemUiModel,
    shareIcon: ShareIcon?,
    canLoadExternalImages: Boolean
) {
    when (item.category) {
        ItemCategory.Note -> NoteRow(item = item)
        ItemCategory.CreditCard -> CreditCardRow(item = item)
        ItemCategory.Identity -> IdentityRow(item = item)
        ItemCategory.Login -> LoginRow(
            item = item,
            vaultIcon = shareIcon?.toSmallResource(),
            canLoadExternalImages = canLoadExternalImages
        )
        ItemCategory.Custom -> CustomRow(item = item)
        ItemCategory.Alias,
        ItemCategory.Password,
        ItemCategory.Unknown -> Unit
    }

}
