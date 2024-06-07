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

package proton.android.pass.features.extrapassword

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordNavItem
import proton.android.pass.features.extrapassword.configure.ui.SetExtraPasswordScreen
import proton.android.pass.features.extrapassword.confirm.navigation.ConfirmExtraPasswordNavItem
import proton.android.pass.features.extrapassword.confirm.ui.ConfirmExtraPasswordDialog
import proton.android.pass.features.extrapassword.infosheet.navigation.ExtraPasswordInfoNavItem
import proton.android.pass.features.extrapassword.infosheet.ui.ExtraPasswordInfoBottomSheet
import proton.android.pass.features.extrapassword.options.navigation.ExtraPasswordOptionsNavItem
import proton.android.pass.features.extrapassword.options.ui.ExtraPasswordOptionsBottomsheet
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

fun NavGraphBuilder.extraPasswordGraph(onNavigate: (ExtraPasswordNavigation) -> Unit) {
    composable(SetExtraPasswordNavItem) {
        SetExtraPasswordScreen(onNavigate = onNavigate)
    }
    bottomSheet(ExtraPasswordOptionsNavItem) {
        ExtraPasswordOptionsBottomsheet(onNavigate = onNavigate)
    }
    bottomSheet(ExtraPasswordInfoNavItem) {
        ExtraPasswordInfoBottomSheet(onNavigate = onNavigate)
    }
    dialog(ConfirmExtraPasswordNavItem) {
        ConfirmExtraPasswordDialog(onNavigate = onNavigate)
    }
}
