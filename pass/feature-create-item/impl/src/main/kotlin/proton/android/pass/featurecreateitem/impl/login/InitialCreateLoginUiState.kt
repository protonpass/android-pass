package proton.android.pass.featurecreateitem.impl.login

import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featurecreateitem.impl.alias.AliasItem

data class InitialCreateLoginUiState(
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageInfoUi: PackageInfoUi? = null,
    val aliasItem: AliasItem? = null,
    val primaryTotp: String? = null
)
