package proton.android.pass.ui.vault

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.CreateVaultScreen
import proton.android.pass.featurevault.impl.VaultListScreen
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.vaultGraph(appNavigator: AppNavigator) {
    composable(AppNavItem.VaultList) {
        VaultListScreen(
            onCreateVault = { appNavigator.navigate(AppNavItem.CreateVault) },
            onEditVault = {},
            onUpClick = appNavigator::onBackClick
        )
    }
    composable(AppNavItem.CreateVault) {
        CreateVaultScreen(
            onUpClick = appNavigator::onBackClick
        )
    }
}
