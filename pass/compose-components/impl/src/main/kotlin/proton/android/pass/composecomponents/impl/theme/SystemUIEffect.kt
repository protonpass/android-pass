package proton.android.pass.composecomponents.impl.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SystemUIEffect(
    isDark: Boolean,
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    LaunchedEffect(systemUiController, isDark) {
        systemUiController.systemBarsDarkContentEnabled = !isDark
    }
}
