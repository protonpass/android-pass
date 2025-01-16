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

package proton.android.pass.features.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

private const val SETTINGS_GRAPH = "settings_graph"

object Settings : NavItem(
    baseRoute = "settings",
    baseDeepLinkRoute = listOf("settings")
)
object LogView : NavItem(baseRoute = "log/view")
object ThemeSelector : NavItem(
    baseRoute = "theme/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object ClipboardSettings : NavItem(
    baseRoute = "clipboard/settings/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object ClearClipboardOptions : NavItem(
    baseRoute = "clipboard/clearOptions/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

sealed interface SettingsNavigation {
    data object SelectTheme : SettingsNavigation
    data object ClipboardSettings : SettingsNavigation
    data object ClearClipboardSettings : SettingsNavigation
    data object CloseScreen : SettingsNavigation
    data object DismissBottomSheet : SettingsNavigation
    data object ViewLogs : SettingsNavigation
    data object Restart : SettingsNavigation
    data object SyncDialog : SettingsNavigation
}

fun NavGraphBuilder.settingsGraph(onNavigate: (SettingsNavigation) -> Unit) {
    navigation(
        route = SETTINGS_GRAPH,
        startDestination = Settings.route
    ) {
        composable(Settings) {
            SettingsScreen(
                onNavigate = onNavigate
            )
        }
        bottomSheet(ThemeSelector) {
            ThemeSelectionBottomSheet(onNavigate = onNavigate)
        }
        composable(LogView) {
            LogViewScreen(onUpClick = { onNavigate(SettingsNavigation.CloseScreen) })
        }
        bottomSheet(ClipboardSettings) {
            ClipboardBottomSheet(
                onClearClipboardSettingClick = { onNavigate(SettingsNavigation.ClearClipboardSettings) }
            )
        }
        bottomSheet(ClearClipboardOptions) {
            ClearClipboardOptionsBottomSheet(onNavigate = onNavigate)
        }
    }
}
