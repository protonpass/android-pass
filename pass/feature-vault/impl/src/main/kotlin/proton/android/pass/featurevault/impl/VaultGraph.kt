package proton.android.pass.featurevault.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object VaultList : NavItem(baseRoute = "vault", isTopLevel = true)
object CreateVault : NavItem(baseRoute = "vault/create")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.vaultGraph(
    onNavigateToCreateVault: () -> Unit,
    onNavigateUp: () -> Unit
) {
    composable(VaultList) {
        VaultListScreen(
            onCreateVault = onNavigateToCreateVault,
            onEditVault = {},
            onUpClick = onNavigateUp
        )
    }
    composable(CreateVault) {
        CreateVaultScreen(
            onUpClick = onNavigateUp
        )
    }
}
