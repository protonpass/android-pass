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

package proton.android.pass.features.item.details.detailforbidden.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsActionForbiddenReason
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.features.item.details.detailforbidden.navigation.ItemDetailsForbiddenReasonNavArgId
import javax.inject.Inject

@HiltViewModel
class ItemDetailsForbiddenViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val reason: ItemDetailsActionForbiddenReason = savedStateHandleProvider.get()
        .require(ItemDetailsForbiddenReasonNavArgId.key)

    internal val state = ItemDetailsForbiddenState(reason = reason)

}
