package proton.android.pass.featurevault.impl

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.pass.domain.ShareId

data class VaultListUIState(
    val list: ImmutableList<ShareUiModel> = persistentListOf(),
    val currentShare: ShareId? = null
)
