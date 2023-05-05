package proton.android.pass.featureauth.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Auth : NavItem(baseRoute = "auth", isTopLevel = true)

sealed interface AuthNavigation {
    object Success : AuthNavigation
    object Failed : AuthNavigation
    object Dismissed : AuthNavigation
    object Back : AuthNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.authGraph(
    navigation: (AuthNavigation) -> Unit
) {
    composable(Auth) {
        BackHandler { navigation(AuthNavigation.Back) }
        AuthScreen(
            navigation = navigation,
        )
    }
}
