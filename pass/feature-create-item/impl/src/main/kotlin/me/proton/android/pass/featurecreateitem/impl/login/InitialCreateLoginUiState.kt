package me.proton.android.pass.featurecreateitem.impl.login

import me.proton.android.pass.featurecreateitem.impl.alias.AliasItem
import me.proton.pass.domain.entity.PackageName

data class InitialCreateLoginUiState(
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageName: PackageName? = null,
    val aliasItem: AliasItem? = null
)
