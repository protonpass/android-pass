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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.itemcreate.R

@Composable
internal fun SelectMailboxesContent(modifier: Modifier = Modifier, state: SelectMailboxesUiState) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.alias_mailbox_dialog_title)
        )

        val list = state.mailboxes.map { mailbox ->
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        BottomSheetItemTitle(
                            text = mailbox.model.email,
                            color = ProtonTheme.colors.textNorm
                        )
                    }
                override val subtitle: @Composable (() -> Unit)? = null
                override val leftIcon: @Composable (() -> Unit)? = null
                override val endIcon: @Composable (() -> Unit)? = null
                override val onClick: (() -> Unit)? = null
                override val isDivider: Boolean = false
            }
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
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
            SelectMailboxesContent(
                state = input.second
            )
        }
    }
}
