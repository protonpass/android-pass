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

package proton.android.pass.features.alias.contacts

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.alias.contacts.create.navigation.CreateAliasContactNavItem
import proton.android.pass.features.alias.contacts.create.ui.CreateAliasContactScreen
import proton.android.pass.features.alias.contacts.detail.navigation.DetailAliasContactNavItem
import proton.android.pass.features.alias.contacts.detail.ui.DetailAliasContactScreen
import proton.android.pass.features.alias.contacts.onboarding.navigation.OnBoardingAliasContactNavItem
import proton.android.pass.features.alias.contacts.onboarding.presentation.OnBoardingAliasContactBottomsheet
import proton.android.pass.features.alias.contacts.options.navigation.OptionsAliasContactNavItem
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasContactBottomSheet
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

fun NavGraphBuilder.aliasContactGraph(onNavigate: (AliasContactsNavigation) -> Unit) {
    composable(CreateAliasContactNavItem) {
        CreateAliasContactScreen(onNavigate = onNavigate)
    }
    composable(DetailAliasContactNavItem) {
        DetailAliasContactScreen(onNavigate = onNavigate)
    }
    bottomSheet(OnBoardingAliasContactNavItem) {
        OnBoardingAliasContactBottomsheet(onNavigate = onNavigate)
    }
    bottomSheet(OptionsAliasContactNavItem) {
        OptionsAliasContactBottomSheet(onNavigate = onNavigate)
    }
}
