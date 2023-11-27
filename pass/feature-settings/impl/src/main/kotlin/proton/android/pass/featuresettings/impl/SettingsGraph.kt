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

package proton.android.pass.featuresettings.impl

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.featuresettings.impl.defaultvault.SelectDefaultVaultBottomSheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

private const val SETTINGS_GRAPH = "settings_graph"

object Settings : NavItem(baseRoute = "settings")
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

object DefaultVault : NavItem(
    baseRoute = "settings/defaultVault",
    navItemType = NavItemType.Bottomsheet
)

sealed interface SettingsNavigation {
    object SelectTheme : SettingsNavigation
    object ClipboardSettings : SettingsNavigation
    object ClearClipboardSettings : SettingsNavigation
    object Close : SettingsNavigation
    object DefaultVault : SettingsNavigation
    object DismissBottomSheet : SettingsNavigation
    object ViewLogs : SettingsNavigation
    object Restart : SettingsNavigation
    object SyncDialog : SettingsNavigation
}

fun NavGraphBuilder.settingsGraph(
    onNavigate: (SettingsNavigation) -> Unit
) {
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
            BackHandler { onNavigate(SettingsNavigation.DismissBottomSheet) }
            ThemeSelectionBottomSheet(onNavigate = onNavigate)
        }
        composable(LogView) {
            LogViewScreen(onUpClick = { onNavigate(SettingsNavigation.Close) })
        }
        bottomSheet(ClipboardSettings) {
            BackHandler { onNavigate(SettingsNavigation.DismissBottomSheet) }
            ClipboardBottomSheet(
                onClearClipboardSettingClick = { onNavigate(SettingsNavigation.ClearClipboardSettings) },
            )
        }
        bottomSheet(ClearClipboardOptions) {
            BackHandler { onNavigate(SettingsNavigation.DismissBottomSheet) }
            ClearClipboardOptionsBottomSheet(onNavigate = onNavigate)
        }

        bottomSheet(DefaultVault) {
            BackHandler { onNavigate(SettingsNavigation.DismissBottomSheet) }
            SelectDefaultVaultBottomSheet(onNavigate = onNavigate)
        }
    }
}
