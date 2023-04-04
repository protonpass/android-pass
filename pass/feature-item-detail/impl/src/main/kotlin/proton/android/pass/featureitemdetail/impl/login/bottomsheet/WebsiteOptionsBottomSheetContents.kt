package proton.android.pass.featureitemdetail.impl.login.bottomsheet

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.featureitemdetail.impl.R

@Composable
fun WebsiteOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    website: String,
    onCopyToClipboard: (String) -> Unit,
    onOpenWebsite: (String) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = persistentListOf(
            openWebsite(onClick = { onOpenWebsite(website) }),
            bottomSheetDivider(),
            copyWebsite(onClick = { onCopyToClipboard(website) })
        )
    )
}

private fun openWebsite(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_open)) }
        override val subtitle: @Composable (() -> Unit)?
            get() = null
        override val leftIcon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_arrow_out_square) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onClick() }
        override val isDivider = false
    }

private fun copyWebsite(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_copy)) }
        override val subtitle: @Composable (() -> Unit)?
            get() = null
        override val leftIcon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_squares) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onClick() }
        override val isDivider = false
    }

@Preview
@Composable
fun LoginDetailBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            WebsiteOptionsBottomSheetContents(
                website = "https://example.local",
                onCopyToClipboard = {},
                onOpenWebsite = {}
            )
        }
    }
}
