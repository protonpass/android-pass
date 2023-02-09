package proton.android.pass.autofill.ui.autosave

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.ui.autosave.save.SAVE_ITEM_ROUTE
import proton.android.pass.autofill.ui.autosave.save.SaveItemScreen
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.preferences.ThemePreference

private const val AUTH_SCREEN_ROUTE = "common/auth"

@OptIn(ExperimentalAnimationApi::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AutoSaveApp(
    info: SaveInformation,
    onAutoSaveSuccess: () -> Unit,
    onAutoSaveCancel: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    val viewModel = hiltViewModel<AutoSaveAppViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = when (state) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> isNightMode()
    }
    ProtonTheme(isDark = isDark) {
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
}
