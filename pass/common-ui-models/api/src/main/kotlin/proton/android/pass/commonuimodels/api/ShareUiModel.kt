package proton.android.pass.commonuimodels.api

import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId

data class ShareUiModel(
    val id: ShareId,
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon
)

data class ShareUiModelWithItemCount(
    val id: ShareId,
    val name: String,
    val activeItemCount: Long,
    val trashedItemCount: Long,
    val color: ShareColor,
    val icon: ShareIcon
)
