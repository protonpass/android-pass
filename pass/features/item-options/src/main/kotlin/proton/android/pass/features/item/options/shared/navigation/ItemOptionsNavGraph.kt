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

package proton.android.pass.features.item.options.shared.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.item.options.aliases.trash.dialogs.navigation.ItemOptionsAliasTrashDialogNavItem
import proton.android.pass.features.item.options.aliases.trash.dialogs.ui.ItemOptionsAliasTrashDialog
import proton.android.pass.features.item.options.confirmresethistory.navigation.ConfirmResetHistoryDialogNavItem
import proton.android.pass.features.item.options.confirmresethistory.ui.ConfirmResetHistoryDialog
import proton.android.pass.navigation.api.dialog

fun NavGraphBuilder.itemOptionsNavGraph(onNavigated: (ItemOptionsNavDestination) -> Unit) {

    dialog(navItem = ItemOptionsAliasTrashDialogNavItem) {
        ItemOptionsAliasTrashDialog(onNavigated = onNavigated)
    }

    dialog(navItem = ConfirmResetHistoryDialogNavItem) {
        ConfirmResetHistoryDialog(onNavigated = onNavigated)
    }
}
