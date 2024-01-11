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
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.pinning.CircledPin
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemdetail.impl.common.ItemTitleInput
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.ThemeItemTitleProvider
import proton.android.pass.featureitemdetail.impl.common.VaultNameSubtitle

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    name: String,
    note: String,
    vault: Vault?,
    moreInfoUiState: MoreInfoUiState,
    onVaultClick: () -> Unit,
    isPinned: Boolean,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                ItemTitleText(text = name, maxLines = Int.MAX_VALUE)
            }

            VaultNameSubtitle(vault = vault, onClick = onVaultClick)
        }

        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = note,
                style = ProtonTheme.typography.defaultNorm
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
                name = params.title,
                note = params.note,
                vault = input.second.vault,
                // We don't care about the MoreInfo as we are not showing it
                moreInfoUiState = MoreInfoUiState.Initial,
                onVaultClick = {},
                isPinned = params.isPinned,
            )
        }
    }
}
