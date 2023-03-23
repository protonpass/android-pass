package proton.android.pass.featuresettings.impl

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.ClearClipboardPreference.Never
import proton.android.pass.preferences.ClearClipboardPreference.S180
import proton.android.pass.preferences.ClearClipboardPreference.S69

@Composable
fun ClearClipboardOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    clearClipboardPreference: ClearClipboardPreference,
    onClearClipboardSettingSelected: (ClearClipboardPreference) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheetPadding(),
        items = clearClipboardItemList(
            clearClipboardPreference,
            onClearClipboardSettingSelected
        ).flatMap { listOf(it, bottomSheetDivider()) }
            .dropLast(1)
            .toPersistentList()
    )
}

private fun clearClipboardItemList(
    clearClipboardPreference: ClearClipboardPreference,
    onClearClipboardPreferenceSelected: (ClearClipboardPreference) -> Unit
): ImmutableList<BottomSheetItem> =
    listOf(S69, S180, Never)
        .map {
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        val clearClipboardString = when (it) {
                            Never -> stringResource(R.string.clipboard_option_clear_clipboard_never)
                            S69 -> stringResource(R.string.clipboard_option_clear_clipboard_after_69_seconds)
                            S180 -> stringResource(R.string.clipboard_option_clear_clipboard_after_180_seconds)
                        }
                        val color = if (it == clearClipboardPreference) {
                            PassTheme.colors.accentBrandNorm
                        } else {
                            PassTheme.colors.textNorm
                        }
                        BottomSheetItemTitle(text = clearClipboardString, color = color)
                    }
                override val subtitle: @Composable (() -> Unit)?
                    get() = null
                override val leftIcon: @Composable (() -> Unit)?
                    get() = null
                override val endIcon: @Composable (() -> Unit)?
                    get() = if (it == clearClipboardPreference) {
                        {
                            BottomSheetItemIcon(
                                iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark,
                                tint = PassTheme.colors.accentBrandOpaque
                            )
                        }
                    } else null
                override val onClick: () -> Unit
                    get() = { onClearClipboardPreferenceSelected(it) }
                override val isDivider = false
            }
        }
        .toImmutableList()

@Preview
@Composable
fun ClearClipboardOptionsBSContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ClearClipboardOptionsBottomSheetContents(clearClipboardPreference = S180) {}
        }
    }
}
