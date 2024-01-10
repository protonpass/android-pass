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
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.domain.entity.PackageInfo

@Immutable
data class AutofillAppState(
    val autofillData: AutofillData
) {
    fun updateAutofillFields(): Pair<Option<PackageInfo>, Option<String>> {
        val packageInfo = autofillData.packageInfo
        val url = autofillData.assistInfo.url

        if (packageInfo.packageName.isBrowser()) {
            return None to url
        }

        // We are sure it's not a browser
        if (url.value().isNullOrBlank()) {
            return packageInfo.some() to None
        }

        // It's not a browser and we have a url, then the URL takes precedence
        return None to url
    }
}

fun AutofillAppState.isValid(): Boolean = autofillData.assistInfo.cluster != NodeCluster.Empty
