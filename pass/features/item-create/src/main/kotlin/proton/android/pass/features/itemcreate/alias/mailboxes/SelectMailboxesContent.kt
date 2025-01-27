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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.banner.AliasCustomDomainBanner
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SelectMailboxesContent(
    modifier: Modifier = Modifier,
    state: SelectMailboxesUiState,
    onEvent: (SelectMailboxEvent) -> Unit
) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.alias_mailbox_dialog_title)
        )

        val list = state.mailboxes.map { mailbox ->
            val isSelected = mailbox in state.selectedMailboxes
            val color = if (isSelected) {
                PassTheme.colors.aliasInteractionNormMajor2
            } else {
                ProtonTheme.colors.textNorm
            }
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        BottomSheetItemTitle(
                            modifier = Modifier.padding(vertical = Spacing.small),
                            text = mailbox.email,
                            color = color
                        )
                    }
                override val subtitle: @Composable (() -> Unit)? = null
                override val leftIcon: @Composable (() -> Unit)? = null
                override val endIcon: @Composable (() -> Unit)? = if (isSelected) {
                    {
                        BottomSheetItemIcon(
                            iconId = CoreR.drawable.ic_proton_checkmark,
                            tint = color
                        )
                    }
                } else null
                override val onClick: () -> Unit = {
                    onEvent(SelectMailboxEvent.SelectMailbox(mailbox))
                }
                override val isDivider: Boolean = false
            }
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
        )
        AnimatedVisibility(state.shouldDisplayFeatureDiscoveryBanner) {
            AliasCustomDomainBanner(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                onClick = {
                    onEvent(SelectMailboxEvent.AddMailbox)
                },
                onClose = {
                    onEvent(SelectMailboxEvent.DismissFeatureDiscoveryBanner)
                }
            )
        }
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
                state = input.second,
                onEvent = {}
            )
        }
    }
}
