package me.proton.core.pass.presentation.components.model

import me.proton.core.pass.domain.ShareId

data class ShareUiModel(
    val id: ShareId,
    val name: String,
)
