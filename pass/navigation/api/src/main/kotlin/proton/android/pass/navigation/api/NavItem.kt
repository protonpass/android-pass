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

package proton.android.pass.navigation.api

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import proton.android.pass.common.api.SpecialCharacters.AMPERSAND
import proton.android.pass.common.api.SpecialCharacters.QUESTION_MARK
import proton.android.pass.common.api.SpecialCharacters.SLASH

open class NavItem(
    val baseRoute: String,
    baseDeepLinkRoute: List<String> = emptyList(),
    private val navArgIds: List<NavArgId> = emptyList(),
    private val optionalArgIds: List<OptionalNavArgId> = emptyList(),
    val noHistory: Boolean = false,
    val isTopLevel: Boolean = false,
    val navItemType: NavItemType = NavItemType.Screen
) {
    val route: String = buildString {
        val argKeys = navArgIds.map(NavArgId::toPathParam)
        append(listOf(baseRoute).plus(argKeys).joinToString("$SLASH"))
        if (optionalArgIds.isNotEmpty()) {
            val optionalArgKeys = optionalArgIds.joinToString(
                prefix = "$QUESTION_MARK",
                separator = "$AMPERSAND",
                transform = OptionalNavArgId::toQueryParam
            )
            append(optionalArgKeys)
        }
    }

    val args: List<NamedNavArgument> =
        navArgIds.map {
            navArgument(it.key) {
                type = it.navType
                if (it.default != null) {
                    defaultValue = it.default
                }
            }
        }
            .plus(
                optionalArgIds.map {
                    navArgument(it.key) {
                        if (it.navType.isNullableAllowed) {
                            nullable = true
                        }
                        if (it.default != null) {
                            defaultValue = it.default
                        }
                        type = it.navType
                    }
                }
            )

    val deepLinkRoutes: List<NavDeepLink> = baseDeepLinkRoute.map {
        val uri = Uri.Builder()
            .scheme(NAV_SCHEME)
            .encodedPath("/internal/$it")
            .encodedQuery(
                navArgIds.joinToString(
                    separator = "$AMPERSAND"
                ) { argId -> argId.toQueryParam() }
            )
        navDeepLink {
            uriPattern = "$uri"
        }
    }
}

const val NAV_SCHEME = "pass_app"

enum class NavItemType {
    Screen, Bottomsheet, Dialog
}
