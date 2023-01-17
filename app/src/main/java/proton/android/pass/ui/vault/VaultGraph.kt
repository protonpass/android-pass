package proton.android.pass.ui.vault

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultScreen
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.vaultGraph() {
    composable(AppNavItem.VaultList) {
        VaultScreen()
    }
}
