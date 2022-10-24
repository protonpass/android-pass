package me.proton.pass.autofill.ui.autosave

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.entities.SaveInformation
import me.proton.pass.autofill.ui.auth.AUTH_SCREEN_ROUTE
import me.proton.pass.autofill.ui.auth.AuthScreen
import me.proton.pass.autofill.ui.autosave.save.SAVE_ITEM_ROUTE
import me.proton.pass.autofill.ui.autosave.save.SaveItemScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AutosaveApp(
    info: SaveInformation,
    onFinished: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    ProtonTheme {
        AnimatedNavHost(
            navController,
            startDestination = AUTH_SCREEN_ROUTE,
            modifier = Modifier.defaultMinSize(minHeight = 200.dp)
        ) {
            composable(AUTH_SCREEN_ROUTE) {
                AuthScreen(
                    onAuthSuccessful = {
                        navController.navigate(SAVE_ITEM_ROUTE) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(SAVE_ITEM_ROUTE) {
                SaveItemScreen(
                    modifier = Modifier,
                    info = info,
                    onSaved = {
                        onFinished()
                    }
                )
            }
        }
    }
}
