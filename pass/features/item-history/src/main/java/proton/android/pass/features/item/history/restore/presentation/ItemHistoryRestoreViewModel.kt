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

package proton.android.pass.features.item.history.restore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.domain.ShareId
import proton.android.pass.features.item.history.navigation.ItemHistoryRevisionNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class ItemHistoryRestoreViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    openItemRevision: OpenItemRevision,
    encryptionContextProvider: EncryptionContextProvider,
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let { id -> ShareId(id = id) }

    // This will be uncommented and used in the following MR

//    private val itemId: ItemId = savedStateHandleProvider.get()
//        .require<String>(CommonNavArgId.ItemId.key)
//        .let { id -> ItemId(id = id) }

    private val itemRevision: ItemRevision = savedStateHandleProvider.get()
        .require<String>(ItemHistoryRevisionNavArgId.key)
        .let { encodedRevision -> Json.decodeFromString(NavParamEncoder.decode(encodedRevision)) }

    internal val state: StateFlow<ItemHistoryRestoreState> = oneShot {
        openItemRevision(shareId, itemRevision)
            .let { item ->
                encryptionContextProvider.withEncryptionContext {
                    item.toUiModel(this@withEncryptionContext)
                }
            }
            .let { itemUiModel ->
                ItemHistoryRestoreState(
                    itemUiModel = itemUiModel,
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ItemHistoryRestoreState(),
    )

}
