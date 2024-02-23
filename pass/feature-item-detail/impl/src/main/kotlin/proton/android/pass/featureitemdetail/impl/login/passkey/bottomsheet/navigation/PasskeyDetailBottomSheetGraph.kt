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

package proton.android.pass.featureitemdetail.impl.login.passkey.bottomsheet.navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemdetail.impl.login.passkey.bottomsheet.ui.PasskeyDetailBottomSheet
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object PasskeyIdNavArgId : NavArgId {
    override val key: String = "passkeyId"
    override val navType: NavType<*> = NavType.StringType
}


object ViewPasskeyDetailsBottomSheet : NavItem(
    baseRoute = "item/detail/login/passkey/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId, PasskeyIdNavArgId)
) {
    fun buildRoute(shareId: ShareId, itemId: ItemId, passkeyId: PasskeyId) =
        "$baseRoute/${shareId.id}/${itemId.id}/${passkeyId.value}"
}

fun NavGraphBuilder.passkeyDetailBottomSheetGraph(
    onDismiss: () -> Unit
) {
    bottomSheet(ViewPasskeyDetailsBottomSheet) {
        BackHandler(onBack = onDismiss)
        PasskeyDetailBottomSheet(onDismiss = onDismiss)
    }
}
