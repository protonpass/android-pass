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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.get
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import proton.android.pass.common.api.SpecialCharacters

fun createDeepLinkUri(path: String): Uri = Uri.Builder()
    .scheme("pass_app")
    .encodedPath(path.trimStart(SpecialCharacters.SLASH))
    .build()

fun NavGraphBuilder.composable(navItem: NavItem, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(
        route = navItem.route,
        arguments = navItem.args,
        deepLinks = navItem.deepLinks.map {
            navDeepLink {
                uriPattern = createDeepLinkUri(it).toString()
            }
        }
    ) {
        content(it)
    }
}

fun NavGraphBuilder.bottomSheet(navItem: NavItem, content: @Composable (NavBackStackEntry) -> Unit) {
    passBottomSheet(
        route = navItem.route,
        arguments = navItem.args
    ) { content(it) }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.passBottomSheet(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
) {
    addDestination(
        PassBottomSheetNavigator.Destination(
            provider[PassBottomSheetNavigator::class],
            content
        ).apply {
            this.route = route
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}

fun NavGraphBuilder.dialog(
    navItem: NavItem,
    dialogProperties: DialogProperties = DialogProperties(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    dialog(
        route = navItem.route,
        arguments = navItem.args,
        dialogProperties = dialogProperties
    ) { content(it) }
}

fun Map<String, Any>.toPath() = this
    .map { "${it.key}=${it.value}" }
    .joinToString(
        prefix = "?",
        separator = "&"
    )
