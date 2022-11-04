package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillMappings
import me.proton.pass.autofill.ui.auth.AUTH_SCREEN_ROUTE
import me.proton.pass.autofill.ui.auth.AuthScreen
import me.proton.pass.autofill.ui.autofill.select.SELECT_ITEM_ROUTE
import me.proton.pass.autofill.ui.autofill.select.SelectItemInitialState
import me.proton.pass.autofill.ui.autofill.select.SelectItemScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    state: AutofillAppState,
    onAutofillResponse: (AutofillMappings?) -> Unit,
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

    val startDestination = if (uiState.isFingerprintRequired) {
        AUTH_SCREEN_ROUTE
    } else {
        SELECT_ITEM_ROUTE
    }

    ProtonTheme(isDark = isDark) {
        AnimatedNavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = navController,
            startDestination = startDestination
        ) {
            composable(AUTH_SCREEN_ROUTE) {
                AuthScreen(
                    onAuthSuccessful = {
                        navController.navigate(SELECT_ITEM_ROUTE) {
                            popUpTo(0)
                        }
                    },
                    onAuthFailed = { onFinished() }
                )
            }
            composable(SELECT_ITEM_ROUTE) {
                SelectItemScreen(
                    initialState = SelectItemInitialState(
                        packageName = state.packageName,
                        webDomain = state.webDomain
                    ),
                    onItemSelected = { autofillItem ->
                        val response = ItemFieldMapper.mapFields(
                            item = autofillItem,
                            androidAutofillFieldIds = state.androidAutofillIds,
                            autofillTypes = state.fieldTypes
                        )
                        onAutofillResponse(response)
                    }
                )
            }
        }
    }
}
