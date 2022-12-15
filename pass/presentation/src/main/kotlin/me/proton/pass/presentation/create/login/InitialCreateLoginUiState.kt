package me.proton.pass.presentation.create.login

import me.proton.pass.domain.entity.PackageName
import me.proton.pass.presentation.create.alias.AliasItem

data class InitialCreateLoginUiState(
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageName: PackageName? = null,
    val aliasItem: AliasItem? = null
)
