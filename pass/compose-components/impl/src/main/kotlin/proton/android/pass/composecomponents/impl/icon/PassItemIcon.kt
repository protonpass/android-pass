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

package proton.android.pass.composecomponents.impl.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomItemIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.items.ItemCategory

@Composable
fun PassItemIcon(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    text: String = "",
    website: String = "",
    packageName: String = "",
    canLoadExternalImages: Boolean = false
) {
    when (itemCategory) {
        ItemCategory.Alias -> AliasIcon(
            modifier = modifier
        )

        ItemCategory.CreditCard -> CreditCardIcon(
            modifier = modifier
        )

        ItemCategory.Note -> NoteIcon(
            modifier = modifier
        )

        ItemCategory.Identity -> IdentityIcon(
            modifier = modifier
        )

        ItemCategory.Login -> LoginIcon(
            modifier = modifier,
            text = text,
            website = website,
            packageName = packageName,
            canLoadExternalImages = canLoadExternalImages
        )

        ItemCategory.WifiNetwork,
        ItemCategory.SSHKey,
        ItemCategory.Custom -> CustomItemIcon(
            modifier = modifier
        )

        ItemCategory.Password,
        ItemCategory.Unknown -> Unit
    }
}
