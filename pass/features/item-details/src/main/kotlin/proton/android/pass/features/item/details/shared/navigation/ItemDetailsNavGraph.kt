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

package proton.android.pass.features.item.details.shared.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.item.details.detail.navigation.ItemDetailsNavItem
import proton.android.pass.features.item.details.detail.ui.ItemDetailsScreen
import proton.android.pass.features.item.details.detailforbidden.navigation.ItemDetailsForbiddenNavItem
import proton.android.pass.features.item.details.detailforbidden.ui.ItemDetailsForbiddenDialog
import proton.android.pass.features.item.details.detailleave.navigation.ItemDetailsLeaveNavItem
import proton.android.pass.features.item.details.detailleave.ui.ItemDetailsLeaveDialog
import proton.android.pass.features.item.details.detailmenu.navigation.ItemDetailsMenuNavItem
import proton.android.pass.features.item.details.detailmenu.ui.ItemDetailsMenuBottomSheet
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.ViewPasskeyDetailsBottomSheet
import proton.android.pass.features.item.details.passkey.bottomsheet.ui.PasskeyDetailBottomSheet
import proton.android.pass.features.item.details.qrviewer.navigation.QRViewerNavItem
import proton.android.pass.features.item.details.qrviewer.ui.QRViewerDialog
import proton.android.pass.features.item.details.reusedpass.navigation.LoginItemDetailsReusedPassNavItem
import proton.android.pass.features.item.details.reusedpass.ui.LoginItemDetailReusedPassScreen
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

fun NavGraphBuilder.itemDetailsNavGraph(onNavigated: (ItemDetailsNavDestination) -> Unit) {

    composable(navItem = ItemDetailsNavItem) {
        ItemDetailsScreen(onNavigated = onNavigated)
    }

    bottomSheet(navItem = ItemDetailsMenuNavItem) {
        ItemDetailsMenuBottomSheet(onNavigated = onNavigated)
    }

    dialog(navItem = ItemDetailsForbiddenNavItem) {
        ItemDetailsForbiddenDialog(onNavigated = onNavigated)
    }

    dialog(navItem = ItemDetailsLeaveNavItem) {
        ItemDetailsLeaveDialog(onNavigated = onNavigated)
    }

    dialog(navItem = QRViewerNavItem) {
        QRViewerDialog(onNavigated = onNavigated)
    }

    bottomSheet(ViewPasskeyDetailsBottomSheet) {
        PasskeyDetailBottomSheet(onNavigated = onNavigated)
    }

    composable(navItem = LoginItemDetailsReusedPassNavItem) {
        LoginItemDetailReusedPassScreen(onNavigated = onNavigated)
    }
}
