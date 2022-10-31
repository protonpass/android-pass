package me.proton.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.proton.core.compose.theme.ProtonTheme
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
    onAutofillResponse: (AutofillMappings?) -> Unit
) {
    val navController = rememberAnimatedNavController()
    ProtonTheme {
        AnimatedNavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = navController,
            startDestination = AUTH_SCREEN_ROUTE
        ) {
            composable(AUTH_SCREEN_ROUTE) {
                AuthScreen(
                    onAuthSuccessful = {
                        navController.navigate(SELECT_ITEM_ROUTE) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(SELECT_ITEM_ROUTE) {
                SelectItemScreen(
                    initialState = SelectItemInitialState(
                        packageName = state.packageName,
                        webDomain = state.webDomain
                    ),
                    onItemSelected = {
                        val response = ItemFieldMapper.mapFields(
                            item = it,
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
