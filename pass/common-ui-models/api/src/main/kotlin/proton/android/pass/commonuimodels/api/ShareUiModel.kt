package proton.android.pass.commonuimodels.api

import proton.pass.domain.ShareId

data class ShareUiModel(
    val id: ShareId,
    val name: String
)

data class ShareUiModelWithItemCount(
    val id: ShareId,
    val name: String,
    val activeItemCount: Long,
    val trashedItemCount: Long
)
