package proton.android.pass.featuresettings.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featuresettings.impl.primaryvault.SelectPrimaryVaultBottomSheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Settings : NavItem(baseRoute = "settings")
object LogView : NavItem(baseRoute = "log/view")
object ThemeSelector : NavItem(baseRoute = "theme/bottomsheet")
object ClipboardSettings : NavItem(baseRoute = "clipboard/settings/bottomsheet")
object ClearClipboardOptions : NavItem(baseRoute = "clipboard/clearOptions/bottomsheet")
object SelectPrimaryVault : NavItem(baseRoute = "vault/primary/bottomsheet")

sealed interface SettingsNavigation {
    object SelectTheme : SettingsNavigation
    object ClipboardSettings : SettingsNavigation
    object ClearClipboardSettings : SettingsNavigation
    object Close : SettingsNavigation
    object DismissBottomSheet : SettingsNavigation
    object ViewLogs : SettingsNavigation
    object PrimaryVault : SettingsNavigation
}

@Suppress("LongParameterList")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    onNavigate: (SettingsNavigation) -> Unit
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
        LogViewScreen(onUpClick = { onNavigate(SettingsNavigation.Close) })
    }

    bottomSheet(ClipboardSettings) {
        ClipboardBottomSheet(
            onClearClipboardSettingClick = { onNavigate(SettingsNavigation.ClearClipboardSettings) },
        )
    }

    bottomSheet(ClearClipboardOptions) {
        ClearClipboardOptionsBottomSheet(onNavigate = onNavigate)
    }

    bottomSheet(SelectPrimaryVault) {
        SelectPrimaryVaultBottomSheet(
            onNavigate = onNavigate
        )
    }
}
