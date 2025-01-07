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

package proton.android.pass.features.itemdetail.login.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.leave
import proton.android.pass.composecomponents.impl.bottomsheet.monitorExclude
import proton.android.pass.composecomponents.impl.bottomsheet.monitorInclude
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.itemdetail.common.migrate
import proton.android.pass.features.itemdetail.common.moveToTrash

@Composable
fun LoginTopBarOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    canMigrate: Boolean,
    canMoveToTrash: Boolean,
    canLeave: Boolean,
    onMigrate: () -> Unit,
    onMoveToTrash: () -> Unit,
    isPinned: Boolean,
    onPinned: () -> Unit,
    onUnpinned: () -> Unit,
    onExcludeFromMonitoring: () -> Unit,
    onIncludeInMonitoring: () -> Unit,
    onLeave: () -> Unit,
    isExcludedFromMonitor: Boolean
) {
    buildList {
        if (canMigrate) {
            migrate(onClick = onMigrate).also(::add)
        }

        if (isPinned) {
            unpin(
                action = BottomSheetItemAction.None,
                onClick = onUnpinned
            ).also(::add)
        } else {
            pin(
                action = BottomSheetItemAction.None,
                onClick = onPinned
            ).also(::add)
        }

        if (isExcludedFromMonitor) {
            monitorInclude(
                action = BottomSheetItemAction.None,
                onClick = onIncludeInMonitoring
            ).also(::add)
        } else {
            monitorExclude(
                action = BottomSheetItemAction.None,
                onClick = onExcludeFromMonitoring
            ).also(::add)
        }

        if (canMoveToTrash) {
            moveToTrash(onClick = onMoveToTrash).also(::add)
        }

        if (canLeave) {
            leave(onClick = onLeave).also(::add)
        }
    }.also { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.withDividers().toPersistentList()
        )
    }
}
