package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.Stable
import proton.pass.domain.ShareId

@Stable
sealed interface AliasDraftSavedState {
    object Unknown : AliasDraftSavedState
    data class Success(
        val shareId: ShareId,
        val aliasItem: AliasItem
    ) : AliasDraftSavedState
}
