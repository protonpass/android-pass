package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
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
import me.proton.pass.autofill.ui.autofill.select.SelectItemInitialState
import me.proton.pass.autofill.ui.autofill.select.SelectItemScreen
import me.proton.pass.autofill.ui.composable
import me.proton.pass.presentation.auth.AuthScreen
import me.proton.pass.presentation.create.alias.CreateAlias
import me.proton.pass.presentation.create.alias.InitialCreateAliasUiState
import me.proton.pass.presentation.create.alias.RESULT_CREATED_ALIAS
import me.proton.pass.presentation.create.login.CreateLogin
import me.proton.pass.presentation.create.login.InitialCreateLoginUiState

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
            composable(AutofillNavItem.Auth) {
                AuthScreen(
                    onAuthSuccessful = {
                        appNavigator.navigate(AutofillNavItem.SelectItem)
                    },
                    onAuthFailed = { onFinished() }
                )
            }
            composable(AutofillNavItem.SelectItem) {
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
                    },
                    onCreateLoginClicked = {
                        appNavigator.navigate(AutofillNavItem.CreateLogin)
                    }
                )
            }
            composable(AutofillNavItem.CreateLogin) {
                val createdAlias by appNavigator.navState<String>(RESULT_CREATED_ALIAS, null)
                    .collectAsStateWithLifecycle()

                CreateLogin(
                    initialContents = InitialCreateLoginUiState(
                        title = state.title,
                        username = createdAlias
                    ),
                    onClose = { appNavigator.onBackClick() },
                    onSuccess = {

                    },
                    onCreateAliasClick = {
                        appNavigator.navigate(
                            AutofillNavItem.CreateAlias,
                            AutofillNavItem.CreateAlias.createNavRoute(it)
                        )
                    }
                )
            }
            composable(AutofillNavItem.CreateAlias) {
                CreateAlias(
                    initialState = InitialCreateAliasUiState(
                        title = state.title
                    ),
                    onSuccess = { alias ->
                        appNavigator.navigateUpWithResult(RESULT_CREATED_ALIAS, alias)
                    },
                    onUpClick = { appNavigator.onBackClick() },
                    onClose = { appNavigator.onBackClick() }
                )
            }
        }
    }
}
