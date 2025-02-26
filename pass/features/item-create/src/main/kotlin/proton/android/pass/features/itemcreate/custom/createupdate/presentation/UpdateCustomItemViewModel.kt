/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import javax.inject.Inject

@HiltViewModel
class UpdateCustomItemViewModel @Inject constructor(
    attachmentsHandler: AttachmentsHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCustomItemViewModel(
    attachmentsHandler = attachmentsHandler,
    encryptionContextProvider = encryptionContextProvider,
    customFieldDraftRepository = customFieldDraftRepository,
    savedStateHandleProvider = savedStateHandleProvider
) {

    init {
        processIntent(UpdateSpecificIntent.LoadInitialData)
    }

    fun processIntent(intent: BaseItemFormIntent) {
        when (intent) {
            is BaseCustomItemCommonIntent -> processCommonIntent(intent)
            is UpdateSpecificIntent -> processSpecificIntent(intent)
            else -> throw IllegalArgumentException("Unknown intent: $intent")
        }
    }

    private fun processSpecificIntent(intent: UpdateSpecificIntent) {
        when (intent) {
            is UpdateSpecificIntent.SubmitUpdate -> onSubmitUpdate()
            is UpdateSpecificIntent.LoadInitialData -> onLoadInitialData()
        }
    }

    private fun onSubmitUpdate() {
        // To implement
    }

    private fun onLoadInitialData() {
        // To implement
    }
}

sealed interface UpdateSpecificIntent : BaseItemFormIntent {
    data object SubmitUpdate : UpdateSpecificIntent
    data object LoadInitialData : UpdateSpecificIntent
}

