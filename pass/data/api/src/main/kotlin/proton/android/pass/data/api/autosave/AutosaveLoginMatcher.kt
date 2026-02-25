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

package proton.android.pass.data.api.autosave

import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.domain.ItemType

data class AutosaveLoginMatcher(
    val username: String,
    val website: String?,
    val packageName: String?
) {

    fun matchesUsername(login: ItemType.Login): Boolean = login.itemUsername == username || login.itemEmail == username

    fun matchesSource(login: ItemType.Login): Boolean? = when {

        packageName != null -> login.packageInfoSet.any {
            it.packageName.value == packageName
        }

        website != null -> {
            val websiteSanitize = UrlSanitizer
                .sanitize(website)
                .getOrNull()
                ?.trimEnd('/')
                ?: return false
            login.websites.any { url ->
                UrlSanitizer.sanitize(url).getOrNull()?.trimEnd('/') == websiteSanitize
            }
        }

        else -> null
    }

}
