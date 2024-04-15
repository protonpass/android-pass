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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItemFormState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState

@Immutable
data class BaseLoginUiState(
    val aliasItemFormState: AliasItemFormState?,
    val validationErrors: PersistentSet<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val openScanState: OpenScanState,
    val focusLastWebsite: Boolean,
    val canUpdateUsername: Boolean,
    val canUseCustomFields: Boolean,
    val primaryEmail: String?,
    val hasUserEditedContent: Boolean,
    val hasReachedAliasLimit: Boolean,
    val totpUiState: TotpUiState,
    val focusedField: LoginField?
) {
    companion object {
        val Initial = BaseLoginUiState(
            aliasItemFormState = null,
            validationErrors = persistentSetOf(),
            isLoadingState = IsLoadingState.NotLoading,
            isItemSaved = ItemSavedState.Unknown,
            openScanState = OpenScanState.Unknown,
            focusLastWebsite = false,
            canUseCustomFields = false,
            canUpdateUsername = true,
            primaryEmail = null,
            hasUserEditedContent = false,
            hasReachedAliasLimit = false,
            totpUiState = TotpUiState.NotInitialised,
            focusedField = null
        )
    }
}

@Immutable
data class CreateLoginUiState(
    val shareUiState: ShareUiState,
    val baseLoginUiState: BaseLoginUiState,
    val passkeyState: Option<CreatePasskeyState> = None
) {
    companion object {
        val Initial = CreateLoginUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseLoginUiState = BaseLoginUiState.Initial
        )
    }
}

@Immutable
data class CreatePasskeyState(
    val domain: String,
    val username: String
)

@Immutable
sealed interface UpdateUiEvent {
    data object Idle : UpdateUiEvent
    data class ConfirmDeletePasskey(val index: Int, val passkey: UIPasskeyContent) : UpdateUiEvent
}

@Immutable
data class UpdateLoginUiState(
    val selectedShareId: ShareId?,
    val baseLoginUiState: BaseLoginUiState,
    val uiEvent: UpdateUiEvent = UpdateUiEvent.Idle
) {
    companion object {
        val Initial = UpdateLoginUiState(
            selectedShareId = null,
            baseLoginUiState = BaseLoginUiState.Initial
        )
    }
}


sealed interface TotpUiState {
    @Immutable
    data object NotInitialised : TotpUiState

    @Immutable
    data object Loading : TotpUiState

    @Immutable
    data object Error : TotpUiState

    @Immutable
    data object Success : TotpUiState

    @Immutable
    data class Limited(val isEdit: Boolean) : TotpUiState
}
