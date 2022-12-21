package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@Composable
fun ThemeSelectionBottomSheetContents(
    modifier: Modifier = Modifier,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(title = R.string.settings_theme_selector_title, showDivider = true)
        BottomSheetItemList(
            items = themeItemList(
                onThemeTypeSelected = onThemeSelected
            )
        )
    }
}

private fun themeItemList(
    onThemeTypeSelected: (ThemePreference) -> Unit
): ImmutableList<BottomSheetItem> =
    ThemePreference.values().map { it.toBottomSheetItem(onThemeTypeSelected) }.toImmutableList()

private fun ThemePreference.toBottomSheetItem(
    onThemeTypeSelected: (ThemePreference) -> Unit
): BottomSheetItem = when (this) {
    ThemePreference.Light -> object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.settings_appearance_preference_subtitle_light)
                )
            }
        override val subtitle: (() -> Unit)?
            get() = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_sun) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
    }
    ThemePreference.Dark -> object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.settings_appearance_preference_subtitle_dark)
                )
            }
        override val subtitle: (() -> Unit)?
            get() = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_moon) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
    }
    ThemePreference.System -> object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.settings_appearance_preference_subtitle_match_system)
                )
            }
        override val subtitle: (() -> Unit)?
            get() = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_cog_wheel) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
    }
}

@Preview
@Composable
fun ThemeSelectionBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            ThemeSelectionBottomSheetContents(onThemeSelected = {})
        }
    }
}
