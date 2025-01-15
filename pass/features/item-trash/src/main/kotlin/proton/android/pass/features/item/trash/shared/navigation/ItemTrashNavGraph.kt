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

package proton.android.pass.features.item.trash.shared.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.item.trash.trashdelete.navigation.ItemTrashDeleteNavItem
import proton.android.pass.features.item.trash.trashdelete.ui.ItemTrashDeleteDialog
import proton.android.pass.features.item.trash.trashmenu.navigation.ItemTrashMenuNavItem
import proton.android.pass.features.item.trash.trashmenu.ui.ItemTrashMenuBottomSheet
import proton.android.pass.features.item.trash.trashwarningshared.navigation.ItemTrashWarningSharedNavItem
import proton.android.pass.features.item.trash.trashwarningshared.ui.ItemTrashWarningSharedDialog
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog

fun NavGraphBuilder.itemTrashNavGraph(onNavigated: (ItemTrashNavDestination) -> Unit) {

    bottomSheet(navItem = ItemTrashMenuNavItem) {
        ItemTrashMenuBottomSheet(onNavigated = onNavigated)
    }

    dialog(navItem = ItemTrashDeleteNavItem) {
        ItemTrashDeleteDialog(onNavigated = onNavigated)
    }

    dialog(navItem = ItemTrashWarningSharedNavItem) {
        ItemTrashWarningSharedDialog(onNavigated = onNavigated)
    }

}
