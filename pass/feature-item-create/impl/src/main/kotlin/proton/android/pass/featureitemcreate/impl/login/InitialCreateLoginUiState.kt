package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureitemcreate.impl.alias.AliasItem

data class InitialCreateLoginUiState(
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageInfoUi: PackageInfoUi? = null,
    val aliasItem: AliasItem? = null,
    val primaryTotp: String? = null
)

val InitialCreateLoginUiStateSaver: Saver<InitialCreateLoginUiState?, Any> = run {
    val title = "title"
    val username = "username"
    val password = "password"
    val url = "url"
    val packageName = "packageName"
    val appName = "appName"
    val aliasItem = "aliasItem"
    val primaryTotp = "primaryTotp"
    mapSaver(
        save = {
            if (it != null) {
                mapOf(
                    title to it.title,
                    username to it.username,
                    password to it.password,
                    url to it.url,
                    packageName to it.packageInfoUi?.packageName,
                    appName to it.packageInfoUi?.appName,
                    aliasItem to it.aliasItem,
                    primaryTotp to it.primaryTotp,
                )
            } else {
                emptyMap()
            }
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                val packageNameValue = values[packageName] as? String
                val appNameValue = values[appName] as? String
                val packageInfoUi = if (packageNameValue != null && appNameValue != null) {
                    PackageInfoUi(packageName = packageNameValue, appName = appNameValue)
                } else {
                    null
                }
                InitialCreateLoginUiState(
                    title = values[title] as? String,
                    username = values[username] as? String,
                    password = values[password] as? String,
                    url = values[url] as? String,
                    packageInfoUi = packageInfoUi,
                    aliasItem = values[aliasItem] as? AliasItem,
                    primaryTotp = values[primaryTotp] as? String
                )
            } else {
                null
            }
        }
    )
}
