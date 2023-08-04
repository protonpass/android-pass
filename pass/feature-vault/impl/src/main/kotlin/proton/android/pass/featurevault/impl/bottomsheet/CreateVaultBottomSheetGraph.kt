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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ShareId

object CreateVaultScreen : NavItem(
    baseRoute = "vault/create/screen",
)

object EditVaultScreen : NavItem(
    baseRoute = "vault/edit/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createNavRoute(shareId: ShareId) = buildString {
        append("$baseRoute/${shareId.id}")
    }
}

@OptIn(ExperimentalAnimationApi::class)
internal fun NavGraphBuilder.createVaultGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    composable(CreateVaultScreen) {
        CreateVaultScreen(
            onNavigate = onNavigate
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
internal fun NavGraphBuilder.editVaultGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    composable(EditVaultScreen) {
        EditVaultScreen(
            onNavigate = onNavigate
        )
    }
}

