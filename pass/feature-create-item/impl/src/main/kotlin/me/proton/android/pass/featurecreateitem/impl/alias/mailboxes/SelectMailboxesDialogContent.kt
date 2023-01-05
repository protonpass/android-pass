package me.proton.android.pass.featurecreateitem.impl.alias.mailboxes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import me.proton.android.pass.composecomponents.impl.uievents.value
import me.proton.android.pass.featurecreateitem.impl.R
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineSmall
import me.proton.core.compose.theme.interactionNorm
import me.proton.pass.commonui.api.ThemedBooleanPreviewProvider
import me.proton.android.pass.featurecreateitem.impl.alias.AliasMailboxUiModel
import me.proton.android.pass.featurecreateitem.impl.alias.SelectedAliasMailboxUiModel

@Composable
fun SelectMailboxesDialogContent(
    modifier: Modifier = Modifier,
    state: SelectMailboxesUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onMailboxToggled: (SelectedAliasMailboxUiModel) -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            ProtonTextButton(
                onClick = onConfirm,
                enabled = state.canApply.value()
            ) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_confirm_button),
                    style = ProtonTheme.typography.headlineSmall,
                    color = ProtonTheme.colors.interactionNorm(enabled = state.canApply.value()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(
                title = stringResource(R.string.alias_mailbox_dialog_cancel_button),
                onClick = onDismiss
            )
        },
        title = {
            ProtonDialogTitle(title = stringResource(R.string.alias_mailbox_dialog_title))
        },
        text = {
            LazyColumn {
                items(items = state.mailboxes, key = { it.model.id }) { item ->
                    SelectMailboxesMailboxRow(
                        item = item,
                        onToggle = { onMailboxToggled(item) }
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun SelectMailboxesDialogContentPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            SelectMailboxesDialogContent(
                state = SelectMailboxesUiState(
                    mailboxes = listOf(
                        SelectedAliasMailboxUiModel(
                            selected = input.second,
                            model = AliasMailboxUiModel(
                                id = 1,
                                email = "eric.norbert@proton.me"
                            )
                        ),
                        SelectedAliasMailboxUiModel(
                            selected = input.second,
                            model = AliasMailboxUiModel(
                                id = 2,
                                email = "eric.work@proton.me"
                            )
                        )
                    ),
                    canApply = IsButtonEnabled.from(input.second)
                ),
                onConfirm = {},
                onDismiss = {},
                onMailboxToggled = {}
            )
        }
    }
}
