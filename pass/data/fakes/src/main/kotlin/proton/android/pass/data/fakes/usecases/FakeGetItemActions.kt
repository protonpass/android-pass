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

package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetItemActions @Inject constructor() : GetItemActions {

    private var result: ItemActions = DEFAULT

    fun emitValue(value: ItemActions) {
        result = value
    }

    override suspend fun invoke(shareId: ShareId, itemId: ItemId) = result

    companion object {
        val DEFAULT = ItemActions(
            canShare = CanShareShareStatus.CanShare(1),
            canEdit = ItemActions.CanEditActionState.Enabled,
            canMoveToOtherVault = ItemActions.CanMoveToOtherVaultState.Enabled,
            canMoveToTrash = true,
            canRestoreFromTrash = false,
            canDelete = true,
            canUseOptions = true
        )
    }

}
