package proton.android.pass.featureprofile.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.featuresettings.impl.SettingsScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    composable(Profile) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(
                    bottomBarSelected = BottomBarSelected.Profile,
                    onListClick = onListClick,
                    onCreateClick = onCreateItemClick,
                    onProfileClick = {}
                )
            }
        ) { padding ->
            SettingsScreen(
                modifier = Modifier.padding(padding),
                onLogoutClick = onLogoutClick
            )
        }
    }
}
