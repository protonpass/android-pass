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

package proton.android.pass.featureitemdetail.impl

import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Left
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

private const val TRANSITION_TIME_MILLIS = 500
private const val FADE_DELAY_TIME_MILLIS = 100

sealed interface ItemDetailNavigation {
    data class OnEdit(val itemUiModel: ItemUiModel) : ItemDetailNavigation

    data class OnMigrate(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailNavigation

    data class OnCreateLoginFromAlias(val alias: String) : ItemDetailNavigation
    data class OnViewItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation
    object Back : ItemDetailNavigation
    object Upgrade : ItemDetailNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : ItemDetailNavigation
}

object ViewItem : NavItem(
    baseRoute = "item",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.itemDetailGraph(
    onNavigate: (ItemDetailNavigation) -> Unit,
) {
    composable(
        navItem = ViewItem,
        enterTransition = {
            fadeIn(tween(TRANSITION_TIME_MILLIS, delayMillis = FADE_DELAY_TIME_MILLIS)) +
                slideIntoContainer(Left, tween(TRANSITION_TIME_MILLIS))
        },
        exitTransition = null,
        popEnterTransition = null,
        popExitTransition = null
    ) {
        ItemDetailScreen(
            onNavigate = onNavigate
        )
    }
}
