package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.animation.AnimatedNavHost
import me.proton.android.pass.navigation.api.rememberAppNavigator
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.isNightMode
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillMappings

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    state: AutofillAppState,
    onAutofillResponse: (AutofillMappings?) -> Unit,
    onFinished: () -> Unit
) {
    val appNavigator = rememberAppNavigator()
    val viewModel: AutofillAppViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.itemSelected is AutofillItemSelectedState.Selected) {
        val selected = uiState.itemSelected
        if (selected is AutofillItemSelectedState.Selected) {
            onAutofillResponse(selected.item)
        }
    }

    val isDark = when (uiState.theme) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> isNightMode()
    }

    val startDestination = if (uiState.isFingerprintRequired) {
        AutofillNavItem.Auth.route
    } else {
        AutofillNavItem.SelectItem.route
    }

    ProtonTheme(isDark = isDark) {
        AnimatedNavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            appGraph(
                appNavigator = appNavigator,
                state = state,
                onAutofillItemClicked = { viewModel.onAutofillItemClicked(state, it) },
                onItemCreated = { viewModel.onItemCreated(state, it) },
                onFinished = onFinished
            )
        }
    }
}
