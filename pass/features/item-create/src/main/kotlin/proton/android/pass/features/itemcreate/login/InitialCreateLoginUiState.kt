/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.login

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.features.itemcreate.alias.AliasItemFormState

data class InitialCreateLoginUiState(
    val title: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageInfoUi: PackageInfoUi? = null,
    val aliasItemFormState: AliasItemFormState? = null,
    val navTotpUri: String? = null,
    val navTotpIndex: Int = -1,
    val passkeyDomain: String? = null,
    val passkeyOrigin: String? = null,
    val passkeyRequest: String? = null
) {

    val passkeyData: PasskeyData? = PasskeyData.buildIfNoneNull(
        domain = passkeyDomain,
        origin = passkeyOrigin,
        request = passkeyRequest
    )

    data class PasskeyData(
        val domain: String,
        val origin: String,
        val request: String
    ) {
        companion object {
            fun from(
                values: Map<String, Any?>,
                domainKey: String,
                originKey: String,
                requestKey: String
            ): PasskeyData? {
                val passkeyDomain = values[domainKey] as? String
                val passkeyOrigin = values[originKey] as? String
                val passkeyRequest = values[requestKey] as? String
                return buildIfNoneNull(domain = passkeyDomain, origin = passkeyOrigin, request = passkeyRequest)
            }

            fun buildIfNoneNull(
                domain: String?,
                origin: String?,
                request: String?
            ): PasskeyData? {
                return if (domain != null && origin != null && request != null) {
                    PasskeyData(domain = domain, origin = origin, request = request)
                } else {
                    null
                }
            }
        }
    }

}

val InitialCreateLoginUiStateSaver: Saver<InitialCreateLoginUiState?, Any> = run {
    val title = "title"
    val email = "email"
    val username = "username"
    val password = "password"
    val url = "url"
    val packageName = "packageName"
    val appName = "appName"
    val aliasItem = "aliasItem"
    val primaryTotp = "primaryTotp"
    val passkeyDomain = "passkeyDomain"
    val passkeyOrigin = "passkeyOrigin"
    val passkeyRequest = "passkeyRequest"
    mapSaver(
        save = {
            if (it != null) {
                mapOf(
                    title to it.title,
                    email to it.email,
                    username to it.username,
                    password to it.password,
                    url to it.url,
                    packageName to it.packageInfoUi?.packageName,
                    appName to it.packageInfoUi?.appName,
                    aliasItem to it.aliasItemFormState,
                    primaryTotp to it.navTotpUri,
                    passkeyOrigin to it.passkeyOrigin,
                    passkeyRequest to it.passkeyRequest
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
                val passkeyData = InitialCreateLoginUiState.PasskeyData.from(
                    values = values,
                    domainKey = passkeyDomain,
                    originKey = passkeyOrigin,
                    requestKey = passkeyRequest
                )

                InitialCreateLoginUiState(
                    title = values[title] as? String,
                    email = values[email] as? String,
                    username = values[username] as? String,
                    password = values[password] as? String,
                    url = values[url] as? String,
                    packageInfoUi = packageInfoUi,
                    aliasItemFormState = values[aliasItem] as? AliasItemFormState,
                    navTotpUri = values[primaryTotp] as? String,
                    passkeyOrigin = passkeyData?.origin,
                    passkeyRequest = passkeyData?.request,
                    passkeyDomain = passkeyData?.domain
                )
            } else {
                null
            }
        }
    )
}
