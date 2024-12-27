/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.alias.mailboxes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel

@Composable
internal fun SelectMailboxesDialogContent(
    modifier: Modifier = Modifier,
    state: SelectMailboxesUiState,
    color: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onMailboxToggled: (SelectedAliasMailboxUiModel) -> Unit
) {
    Column(modifier = modifier) {
        ProtonDialogTitle(
            modifier = Modifier.padding(all = Spacing.medium),
            title = stringResource(R.string.alias_mailbox_dialog_title)
        )

        LazyColumn(Modifier.weight(weight = 1f, fill = false)) {
            items(
                items = state.mailboxes,
                key = { mailboxUiModel -> mailboxUiModel.model.id }
            ) { mailboxUiModel ->
                SelectMailboxesMailboxRow(
                    item = mailboxUiModel,
                    color = color,
                    onToggle = { onMailboxToggled(mailboxUiModel) }
                )
            }
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

internal class ThemedSelectMailboxesPreviewProvider :
    ThemePairPreviewProvider<SelectMailboxesUiState>(SelectMailboxesUiStatePreviewProvider())

@Preview
@Composable
internal fun SelectMailboxesDialogContentPreview(
    @PreviewParameter(ThemedSelectMailboxesPreviewProvider::class) input: Pair<Boolean, SelectMailboxesUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectMailboxesDialogContent(
                state = input.second,
                color = PassTheme.colors.interactionNormMajor2,
                onConfirm = {},
                onDismiss = {},
                onMailboxToggled = {}
            )
        }
    }
}
