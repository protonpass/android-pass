package proton.android.pass.featureitemcreate.impl.alias.suffixes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel

@Composable
fun SelectSuffixDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    canUpgrade: Boolean,
    suffixes: ImmutableList<AliasSuffixUiModel>,
    selectedSuffix: AliasSuffixUiModel?,
    color: Color,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    if (!show) return

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        SelectSuffixContent(
            modifier = Modifier,
            suffixes = suffixes,
            canUpgrade = canUpgrade,
            selectedSuffix = selectedSuffix,
            color = color,
            onSuffixChanged = onSuffixChanged,
            onDismiss = onDismiss,
            onUpgrade = onUpgrade
        )
    }
}
