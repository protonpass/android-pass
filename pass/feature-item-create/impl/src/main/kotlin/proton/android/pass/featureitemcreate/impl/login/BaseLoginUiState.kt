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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItemFormState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId

@Immutable
data class BaseLoginUiState(
    val contents: ItemContents.Login,
    val aliasItemFormState: AliasItemFormState?,
    val validationErrors: PersistentSet<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val openScanState: OpenScanState,
    val focusLastWebsite: Boolean,
    val canUpdateUsername: Boolean,
    val primaryEmail: String?,
    val hasUserEditedContent: Boolean,
    val hasReachedAliasLimit: Boolean,
    val totpUiState: TotpUiState,
    val customFieldsState: CustomFieldsState,
    val focusedField: LoginField?,
) {
    companion object {
        fun create(
            password: HiddenState,
            primaryTotp: HiddenState
        ) = BaseLoginUiState(
            contents = ItemContents.Login.create(password, primaryTotp),
            aliasItemFormState = null,
            validationErrors = persistentSetOf(),
            isLoadingState = IsLoadingState.NotLoading,
            isItemSaved = ItemSavedState.Unknown,
            openScanState = OpenScanState.Unknown,
            focusLastWebsite = false,
            canUpdateUsername = true,
            primaryEmail = null,
            hasUserEditedContent = false,
            hasReachedAliasLimit = false,
            totpUiState = TotpUiState.NotInitialised,
            customFieldsState = CustomFieldsState.NotInitialised,
            focusedField = null
        )
    }
}

@Immutable
data class CreateLoginUiState(
    val shareUiState: ShareUiState,
    val baseLoginUiState: BaseLoginUiState
) {
    companion object {
        fun create(
            password: HiddenState,
            primaryTotp: HiddenState
        ) = CreateLoginUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseLoginUiState = BaseLoginUiState.create(password, primaryTotp)
        )
    }
}

@Immutable
data class UpdateLoginUiState(
    val selectedShareId: ShareId?,
    val baseLoginUiState: BaseLoginUiState
) {
    companion object {
        fun create(
            password: HiddenState,
            primaryTotp: HiddenState
        ) = UpdateLoginUiState(
            selectedShareId = null,
            baseLoginUiState = BaseLoginUiState.create(password, primaryTotp)
        )
    }
}


sealed interface TotpUiState {
    @Immutable
    object NotInitialised : TotpUiState

    @Immutable
    object Loading : TotpUiState

    @Immutable
    object Error : TotpUiState

    @Immutable
    object Success : TotpUiState

    @Immutable
    data class Limited(val isEdit: Boolean) : TotpUiState
}

sealed interface CustomFieldsState {
    @Immutable
    object NotInitialised : CustomFieldsState

    @Immutable
    object Disabled : CustomFieldsState

    @Immutable
    data class Enabled(
        val customFields: List<CustomFieldContent>,
        val isLimited: Boolean
    ) : CustomFieldsState
}
