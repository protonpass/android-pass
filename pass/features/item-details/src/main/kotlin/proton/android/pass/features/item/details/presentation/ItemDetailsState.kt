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

package proton.android.pass.features.item.details.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId

@Stable
internal sealed interface ItemDetailsState {

    data object Error : ItemDetailsState

    data object Loading : ItemDetailsState

    data class Success(
        internal val shareId: ShareId,
        internal val itemId: ItemId,
        internal val itemDetailState: ItemDetailState,
        internal val itemActions: ItemActions,
        private val userPlan: Plan
    ) : ItemDetailsState {

        internal val hasPaidPlan: Boolean = when (userPlan.planType) {
            is PlanType.Paid,
            is PlanType.Trial -> true

            is PlanType.Free,
            is PlanType.Unknown -> false
        }

    }

}
