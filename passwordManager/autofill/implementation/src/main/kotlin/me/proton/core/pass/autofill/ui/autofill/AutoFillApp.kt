package me.proton.core.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.entities.AutofillResponse
import me.proton.core.pass.autofill.entities.SearchCredentialsInfo
import me.proton.core.pass.autofill.ui.autofill.auth.AUTH_SCREEN_ROUTE
import me.proton.core.pass.autofill.ui.autofill.auth.AuthScreen
import me.proton.core.pass.autofill.ui.autofill.select.SELECT_ITEM_ROUTE
import me.proton.core.pass.autofill.ui.autofill.select.SelectItemScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AutofillApp(
    info: SearchCredentialsInfo,
    onAutofillResponse: (AutofillResponse?) -> Unit
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
                        navController.navigate(SELECT_ITEM_ROUTE) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(SELECT_ITEM_ROUTE) {
                SelectItemScreen(
                    onItemSelected = {
                        val response = ItemFieldMapper.mapFields(it, info)
                        onAutofillResponse(response)
                    }
                )
            }
        }
    }
}
