/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.creditcard

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.createAliasGraph
import proton.android.pass.features.itemcreate.alias.updateAliasGraph

fun NavGraphBuilder.createUpdateAliasGraph(
    canUseAttachments: Boolean,
    canAddMailbox: Boolean,
    onNavigate: (BaseAliasNavigation) -> Unit
) {
    createAliasGraph(
        canUseAttachments = canUseAttachments,
        canAddMailbox = canAddMailbox,
        onNavigate = onNavigate
    )
    updateAliasGraph(
        onNavigate = onNavigate
    )
}
