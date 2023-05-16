package proton.android.pass.composecomponents.impl.theme

import androidx.compose.runtime.Composable
import me.proton.core.compose.theme.isNightMode
import proton.android.pass.preferences.ThemePreference

@Composable
fun isDark(preference: ThemePreference) = when (preference) {
    ThemePreference.Dark -> true
    ThemePreference.Light -> false
    ThemePreference.System -> isNightMode()
}
