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

package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.featureitemcreate.impl.alias.bottomsheet.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.ShowUpgradeNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object IsEditAliasNavArg : OptionalNavArgId {
    override val key = "isEdit"
    override val navType = NavType.BoolType
}

object CreateAlias : NavItem(
    baseRoute = "alias/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, AliasOptionalNavArgId.Title)
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
        title: Option<String> = None,
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }
        val path = map.toPath()
        append(path)
    }
}

object CreateAliasBottomSheet : NavItem(
    baseRoute = "alias/create/bottomsheet",
    navArgIds = listOf(CommonOptionalNavArgId.ShareId, ShowUpgradeNavArgId),
    optionalArgIds = listOf(AliasOptionalNavArgId.Title, IsEditAliasNavArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(
        shareId: ShareId,
        showUpgrade: Boolean,
        title: Option<String> = None,
        isEdit: Boolean = false
    ): String = buildString {
        append("$baseRoute/${shareId.id}/$showUpgrade")

        val map = mutableMapOf<String, Any>(
            IsEditAliasNavArg.key to isEdit
        )
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }

        val optionalPath = map.toPath()
        append(optionalPath)
    }
}

sealed interface CreateAliasNavigation {
    data class CreatedFromBottomsheet(val alias: String) : CreateAliasNavigation
    data class Created(
        val shareId: ShareId,
        val itemId: ItemId,
        val alias: String
    ) : CreateAliasNavigation

    object Upgrade : CreateAliasNavigation
    object Close : CreateAliasNavigation
    object CloseBottomsheet : CreateAliasNavigation

    data class SelectVault(val shareId: ShareId) : CreateAliasNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.createAliasGraph(
    onNavigate: (CreateAliasNavigation) -> Unit,
) {
    composable(CreateAlias) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateAliasScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            onNavigate = onNavigate
        )
    }

    bottomSheet(CreateAliasBottomSheet) {
        val itemTitle = it.arguments?.getString(AliasOptionalNavArgId.Title.key) ?: ""
        CreateAliasBottomSheet(
            itemTitle = itemTitle,
            onNavigate = onNavigate
        )
    }
}
