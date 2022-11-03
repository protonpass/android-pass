package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem

@Composable
fun ThemeSelectionBottomSheet(
    modifier: Modifier = Modifier,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_theme_selector_title),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItem(
            icon = me.proton.core.presentation.R.drawable.ic_proton_cog_wheel,
            title = R.string.settings_appearance_preference_subtitle_match_system,
            onItemClick = { onThemeSelected(ThemePreference.System) }
        )
        BottomSheetItem(
            icon = me.proton.core.presentation.R.drawable.ic_proton_moon,
            title = R.string.settings_appearance_preference_subtitle_dark,
            onItemClick = { onThemeSelected(ThemePreference.Dark) }
        )
        BottomSheetItem(
            icon = me.proton.core.presentation.R.drawable.ic_proton_sun,
            title = R.string.settings_appearance_preference_subtitle_light,
            onItemClick = { onThemeSelected(ThemePreference.Light) }
        )
    }
}
