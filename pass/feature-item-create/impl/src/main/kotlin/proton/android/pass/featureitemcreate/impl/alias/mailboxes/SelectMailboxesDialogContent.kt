package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@Composable
fun SelectMailboxesDialogContent(
    modifier: Modifier = Modifier,
    state: SelectMailboxesUiState,
    color: Color,
    onUpgrade: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onMailboxToggled: (SelectedAliasMailboxUiModel) -> Unit
) {
    Column(modifier = modifier) {
        ProtonDialogTitle(
            modifier = Modifier.padding(16.dp),
            title = stringResource(R.string.alias_mailbox_dialog_title)
        )
        LazyColumn {
            items(items = state.mailboxes, key = { it.model.id }) { item ->
                SelectMailboxesMailboxRow(
                    item = item,
                    color = color,
                    onToggle = { onMailboxToggled(item) }
                )
            }
        }
        if (state.canUpgrade) {
            Divider(color = PassTheme.colors.inputBorderNorm)
            Row(
                modifier = Modifier
                    .clickable(onClick = onUpgrade)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.select_mailbox_upgrade_for_more_inboxes),
                    style = ProtonTheme.typography.defaultNorm,
                    color = PassTheme.colors.interactionNormMajor2
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                    contentDescription = stringResource(ComponentsR.string.upgrade_icon_content_description),
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
            Divider(color = PassTheme.colors.inputBorderNorm)
        }
        DialogCancelConfirmSection(
            color = color,
            disabledColor = ProtonTheme.colors.interactionDisabled,
            confirmEnabled = state.canApply.value(),
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

class ThemedSelectMailboxesPreviewProvider :
    ThemePairPreviewProvider<SelectMailboxesUiState>(SelectMailboxesUiStatePreviewProvider())

@Preview
@Composable
fun SelectMailboxesDialogContentPreview(
    @PreviewParameter(ThemedSelectMailboxesPreviewProvider::class) input: Pair<Boolean, SelectMailboxesUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectMailboxesDialogContent(
                state = input.second,
                color = PassTheme.colors.interactionNormMajor2,
                onUpgrade = {},
                onConfirm = {},
                onDismiss = {},
                onMailboxToggled = {},
            )
        }
    }
}
