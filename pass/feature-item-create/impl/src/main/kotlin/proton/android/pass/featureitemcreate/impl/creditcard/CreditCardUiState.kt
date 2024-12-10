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

package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState

@Immutable
data class BaseCreditCardUiState(
    val isLoading: Boolean,
    val hasUserEditedContent: Boolean,
    val validationErrors: PersistentSet<CreditCardValidationErrors>,
    val isItemSaved: ItemSavedState,
    val isDowngradedMode: Boolean,
    val isFileAttachmentsEnabled: Boolean,
    val attachmentsState: AttachmentsState
) {
    companion object {
        val Initial = BaseCreditCardUiState(
            isLoading = false,
            hasUserEditedContent = false,
            validationErrors = persistentSetOf(),
            isItemSaved = ItemSavedState.Unknown,
            isDowngradedMode = false,
            isFileAttachmentsEnabled = false,
            attachmentsState = AttachmentsState.Initial
        )
    }
}

sealed interface CreateCreditCardUiState {
    @Immutable
    data object NotInitialised : CreateCreditCardUiState

    @Immutable
    data object Loading : CreateCreditCardUiState

    @Immutable
    data object Error : CreateCreditCardUiState

    @Immutable
    data class Success(
        val shareUiState: ShareUiState,
        val baseState: BaseCreditCardUiState
    ) : CreateCreditCardUiState
}

sealed interface UpdateCreditCardUiState {
    @Immutable
    data object NotInitialised : UpdateCreditCardUiState

    @Immutable
    data object Loading : UpdateCreditCardUiState

    @Immutable
    data object Error : UpdateCreditCardUiState

    @Immutable
    data class Success(
        val selectedShareId: ShareId?,
        val baseState: BaseCreditCardUiState
    ) : UpdateCreditCardUiState
}
