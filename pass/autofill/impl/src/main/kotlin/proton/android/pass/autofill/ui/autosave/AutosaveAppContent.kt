package proton.android.pass.autofill.ui.autosave

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.ui.autosave.save.SAVE_ITEM_ROUTE
import proton.android.pass.autofill.ui.autosave.save.SaveItemScreen
import proton.android.pass.featureauth.impl.AuthScreen

private const val AUTH_SCREEN_ROUTE = "common/auth"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AutosaveAppContent(
    modifier: Modifier = Modifier,
    info: SaveInformation,
    onAutoSaveSuccess: () -> Unit,
    onAutoSaveCancel: () -> Unit
) {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        navController = navController,
        startDestination = AUTH_SCREEN_ROUTE,
    ) {
        composable(AUTH_SCREEN_ROUTE) {
            AuthScreen(
                onAuthSuccessful = {
                    navController.navigate(SAVE_ITEM_ROUTE) {
                        popUpTo(0)
                    }
                },
                onAuthFailed = { onAutoSaveCancel() },
                onAuthDismissed = { onAutoSaveCancel() }
            )
        }
        composable(SAVE_ITEM_ROUTE) {
            SaveItemScreen(
                info = info,
                onSaved = { onAutoSaveSuccess() }
            )
        }
    }
}
