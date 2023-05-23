package proton.android.pass.featureprofile.impl

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers

@Composable
@Suppress("UnusedPrivateMember")
fun FeedbackBottomsheetContent(
    modifier: Modifier = Modifier,
    onSendReport: () -> Unit,
    onOpenReddit: () -> Unit,
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(
            sendReport(onClick = onSendReport),
            openProtonReddit(onClick = onOpenReddit),
        ).withDividers().toPersistentList()
    )
}

@Preview
@Composable
fun FeedbackBottomsheetContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            FeedbackBottomsheetContent(
                onSendReport = {},
                onOpenReddit = {}
            )
        }
    }
}

private fun sendReport(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(R.string.feedback_option_mail)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_paper_plane) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = onClick
        override val isDivider = false
    }

private fun openProtonReddit(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(R.string.feedback_option_reddit)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_reddit) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = onClick
        override val isDivider = false
    }
