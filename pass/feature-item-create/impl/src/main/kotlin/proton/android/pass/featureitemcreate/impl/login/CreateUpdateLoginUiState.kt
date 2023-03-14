package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Immutable
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem

@Immutable
data class CreateUpdateLoginUiState(
    val shareList: List<ShareUiModel>,
    val selectedShareId: ShareUiModel?,
    val loginItem: LoginItem,
    val aliasItem: AliasItem?,
    val validationErrors: Set<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val openScanState: OpenScanState,
    val focusLastWebsite: Boolean,
    val canUpdateUsername: Boolean,
    val primaryEmail: String?,
    val showVaultSelector: Boolean,
) {
    companion object {
        val Initial = CreateUpdateLoginUiState(
            shareList = emptyList(),
            selectedShareId = null,
            aliasItem = null,
            isLoadingState = IsLoadingState.NotLoading,
            loginItem = LoginItem.Empty,
            validationErrors = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            openScanState = OpenScanState.Unknown,
            focusLastWebsite = false,
            canUpdateUsername = true,
            primaryEmail = null,
            showVaultSelector = false
        )
    }
}
