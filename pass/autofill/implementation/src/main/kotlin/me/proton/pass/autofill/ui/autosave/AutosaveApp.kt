package me.proton.pass.autofill.ui.autosave

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
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import me.proton.pass.autofill.entities.SaveInformation
import me.proton.pass.autofill.ui.autofill.AutofillAppViewModel
import me.proton.pass.autofill.ui.autosave.save.SAVE_ITEM_ROUTE
import me.proton.pass.autofill.ui.autosave.save.SaveItemScreen
import me.proton.pass.presentation.auth.AuthScreen

private const val AUTH_SCREEN_ROUTE = "common/auth"

@OptIn(ExperimentalAnimationApi::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AutosaveApp(
    info: SaveInformation,
    onFinished: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    val viewModel: AutofillAppViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val isDark = when (uiState.theme) {
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
                    onAuthFailed = { onFinished() },
                    onAuthDismissed = { onFinished() }
                )
            }
            composable(SAVE_ITEM_ROUTE) {
                SaveItemScreen(
                    info = info,
                    onSaved = { onFinished() }
                )
            }
        }
    }
}
