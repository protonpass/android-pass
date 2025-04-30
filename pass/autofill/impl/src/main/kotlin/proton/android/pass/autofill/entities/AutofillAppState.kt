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

package proton.android.pass.autofill.entities

import androidx.compose.runtime.Immutable
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.log.api.PassLogger
import java.net.URI

private const val TAG = "AutofillAppState"

@Immutable
internal data class AutofillAppState(
    internal val autofillData: AutofillData,
    private val packageNameUrlSuggestionAdapter: PackageNameUrlSuggestionAdapter
) {
    private val suggestionsTitle: String by lazy {
        when (autofillData.assistInfo.url) {
            None -> {
                autofillData.packageInfo.appName.value
            }

            is Some -> {
                UrlSanitizer.sanitize(autofillData.assistInfo.url.value).fold(
                    onSuccess = { url ->
                        runCatching { URI(url).host }.getOrDefault("")
                    },
                    onFailure = { error ->
                        PassLogger.i(
                            tag = TAG,
                            e = error,
                            message = "Error sanitizing URL [url=${autofillData.assistInfo.url.value}]"
                        )
                        ""
                    }
                )
            }
        }
    }

    internal fun updateAutofillFields(): Pair<Option<PackageInfo>, Option<String>> {
        val packageInfo = autofillData.packageInfo
        val optionUrl = autofillData.assistInfo.url

        if (packageInfo.packageName.isBrowser()) {
            return None to optionUrl
        }

        // We are sure it's not a browser
        if (optionUrl.value().isNullOrBlank()) {
            return packageInfo.some() to None
        }

        // It's not a browser and we have a url, then the URL takes precedence
        return None to optionUrl
    }

    internal fun isValid(): Boolean = autofillData.assistInfo.cluster != NodeCluster.Empty

    internal fun toSelectItemState(): SelectItemState.Autofill {
        return when (autofillData.assistInfo.cluster) {
            is NodeCluster.CreditCard -> {
                SelectItemState.Autofill.CreditCard(title = suggestionsTitle)
            }

            is NodeCluster.Login,
            is NodeCluster.SignUp -> {
                SelectItemState.Autofill.Login(
                    title = suggestionsTitle,
                    suggestion = packageNameUrlSuggestionAdapter.adapt(
                        packageName = autofillData.packageInfo.packageName,
                        url = autofillData.assistInfo.url.value().orEmpty()
                    ).toSuggestion()
                )
            }

            is NodeCluster.Identity -> {
                SelectItemState.Autofill.Identity(title = suggestionsTitle)
            }

            NodeCluster.Empty -> {
                throw IllegalStateException("Empty cluster type")
            }
        }
    }
}
