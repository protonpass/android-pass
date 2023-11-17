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
import androidx.navigation.NavType
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

sealed interface CreateVaultNextAction {

    fun value(): String

    object Done : CreateVaultNextAction {
        override fun value() = NEXT_ACTION_DONE
    }
    data class ShareVault(
        val shareId: ShareId,
        val itemId: ItemId
    ) : CreateVaultNextAction {
        override fun value() = NEXT_ACTION_SHARE
    }

    companion object {
        const val NEXT_ACTION_DONE = "done"
        const val NEXT_ACTION_SHARE = "share"
    }
}

object CreateVaultNextActionNavArgId : NavArgId {
    override val key = "create_vault_next_action"
    override val navType = NavType.StringType
}

object CreateVaultScreen : NavItem(
    baseRoute = "vault/create/screen",
    navArgIds = listOf(CreateVaultNextActionNavArgId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, CommonOptionalNavArgId.ItemId)
) {
    fun buildRoute(nextAction: CreateVaultNextAction) = buildString {
        append("$baseRoute/${nextAction.value()}")
        if (nextAction is CreateVaultNextAction.ShareVault) {
            val extras = mapOf(
                CommonOptionalNavArgId.ShareId.key to nextAction.shareId.id,
                CommonOptionalNavArgId.ItemId.key to nextAction.itemId.id
            )
            append(extras.toPath())
        }
    }
}

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

