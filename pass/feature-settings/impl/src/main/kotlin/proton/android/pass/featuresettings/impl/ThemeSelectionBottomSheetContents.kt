package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.PassDimens.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.preferences.ThemePreference

@Composable
fun ThemeSelectionBottomSheetContents(
    modifier: Modifier = Modifier,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column(modifier = modifier.bottomSheetPadding()) {
        BottomSheetTitle(title = stringResource(id = R.string.settings_theme_selector_title), showDivider = false)
        Spacer(modifier = Modifier.height(12.dp))
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
    ThemePreference.values()
        .map { it.toBottomSheetItem(onThemeTypeSelected) }
        .toImmutableList()

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
        override val subtitle: @Composable (() -> Unit)? = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_sun) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
        override val isDivider = false
    }
    ThemePreference.Dark -> object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.settings_appearance_preference_subtitle_dark)
                )
            }
        override val subtitle: @Composable (() -> Unit)? = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_moon) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
        override val isDivider = false
    }
    ThemePreference.System -> object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.settings_appearance_preference_subtitle_match_system)
                )
            }
        override val subtitle: @Composable (() -> Unit)? = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_cog_wheel) }
        override val onClick: () -> Unit
            get() = { onThemeTypeSelected(this@toBottomSheetItem) }
        override val isDivider = false
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
