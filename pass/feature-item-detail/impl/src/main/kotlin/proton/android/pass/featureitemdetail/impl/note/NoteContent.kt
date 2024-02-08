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

package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.pinning.CircledPin
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemdetail.impl.common.HistorySection
import proton.android.pass.featureitemdetail.impl.common.ItemTitleInput
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.ThemeItemTitleProvider
import proton.android.pass.featureitemdetail.impl.common.VaultNameSubtitle

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    vault: Vault?,
    moreInfoUiState: MoreInfoUiState,
    onVaultClick: () -> Unit,
    isPinned: Boolean,
    onViewItemHistoryClicked: () -> Unit,
    isHistoryFeatureEnabled: Boolean,
) {
    val contents = itemUiModel.contents as ItemContents.Note

    Column(
        modifier = modifier.padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                AnimatedVisibility(
                    visible = isPinned,
                    enter = expandHorizontally(),
                ) {
                    CircledPin(
                        ratio = 1f,
                        backgroundColor = PassTheme.colors.noteInteractionNormMajor1,
                    )
                }

                ItemTitleText(text = contents.title, maxLines = Int.MAX_VALUE)
            }

            VaultNameSubtitle(vault = vault, onClick = onVaultClick)
        }

        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = contents.note,
                style = ProtonTheme.typography.defaultNorm,
            )
        }

        if (isHistoryFeatureEnabled) {
            HistorySection(
                createdInstant = itemUiModel.createTime,
                modifiedInstant = itemUiModel.modificationTime,
                onViewItemHistoryClicked = onViewItemHistoryClicked,
                buttonBackgroundColor = PassTheme.colors.noteInteractionNormMinor2,
                buttonTextColor = PassTheme.colors.noteInteractionNormMajor2,
            )
        }

        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}

@Preview
@Composable
fun NoteContentPreview(
    @PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            NoteContent(
                itemUiModel = params.itemUiModel,
                vault = params.vault,
                // We don't care about the MoreInfo as we are not showing it
                moreInfoUiState = MoreInfoUiState.Initial,
                onVaultClick = {},
                isPinned = params.isPinned,
                onViewItemHistoryClicked = {},
                isHistoryFeatureEnabled = params.isHistoryFeatureEnabled,
            )
        }
    }
}
