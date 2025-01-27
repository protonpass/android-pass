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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.container.roundedContainerStrong
import proton.android.pass.composecomponents.impl.form.ChevronDownIcon
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.icon.ForwardIcon
import proton.android.pass.composecomponents.impl.modifiers.placeholder
import proton.android.pass.features.itemcreate.R

@Composable
fun MailboxSection(
    modifier: Modifier = Modifier,
    selectedMailboxes: ImmutableList<AliasMailboxUiModel>,
    isCreateMode: Boolean,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    isBottomSheet: Boolean,
    onMailboxClick: () -> Unit
) {
    val labelText = if (isCreateMode) {
        stringResource(R.string.field_mailboxes_creation_title)
    } else {
        stringResource(R.string.field_mailboxes_edit_title)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(
                condition = isBottomSheet,
                ifTrue = { roundedContainerStrong() },
                ifFalse = { roundedContainerNorm() }
            )
            .clickable(enabled = isEditAllowed, onClick = onMailboxClick)
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        ForwardIcon(tint = PassTheme.colors.textWeak)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ProtonTextFieldLabel(text = labelText)
            if (isLoading) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(),
                    text = ""
                )
            } else {
                selectedMailboxes.forEach { mailbox ->
                    Text(text = mailbox.email)
                }
            }

        }
        if (isEditAllowed) {
            ChevronDownIcon()
        }
    }
}

@Preview
@Composable
fun MailboxSectionPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            MailboxSection(
                isLoading = false,
                isBottomSheet = false,
                selectedMailboxes = persistentListOf(
                    AliasMailboxUiModel(
                        id = 1,
                        email = "prefix@suffix.test"
                    )
                ),
                isCreateMode = true,
                isEditAllowed = input.second,
                onMailboxClick = {}
            )
        }
    }
}

@Preview
@Composable
fun MailboxSectionBottomSheetPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MailboxSection(
                isBottomSheet = input.second,
                isLoading = false,
                selectedMailboxes = persistentListOf(
                    AliasMailboxUiModel(
                        id = 1,
                        email = "prefix@suffix.test"
                    )
                ),
                isEditAllowed = true,
                isCreateMode = false,
                onMailboxClick = {}
            )
        }
    }
}
