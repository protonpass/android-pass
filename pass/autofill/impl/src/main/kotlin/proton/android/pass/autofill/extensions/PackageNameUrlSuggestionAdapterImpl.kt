/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.autofill.extensions

import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.autofill.api.suggestions.SuggestionSource
import proton.android.pass.domain.entity.PackageName
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageNameUrlSuggestionAdapterImpl @Inject constructor() : PackageNameUrlSuggestionAdapter {

    override fun adapt(packageName: PackageName, url: String): SuggestionSource {
        val autofillDataPackageName = packageName
            .takeIf { !it.isBrowser() }
            ?.value

        return when {
            // App with a webview
            !autofillDataPackageName.isNullOrBlank() && url.isNotBlank() ->
                SuggestionSource.WithUrl(url)

            !autofillDataPackageName.isNullOrBlank() && url.isBlank() ->
                SuggestionSource.WithPackageName(autofillDataPackageName)

            autofillDataPackageName.isNullOrBlank() -> SuggestionSource.WithUrl(url)

            // Should not happen
            else -> throw IllegalStateException("Unexpected state")
        }
    }

}
