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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel

internal class SelectMailboxesUiStatePreviewProvider :
    PreviewParameterProvider<SelectMailboxesUiState> {

    override val values: Sequence<SelectMailboxesUiState> = sequenceOf(
        SelectMailboxesUiState(
            mailboxes = listOf(
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(
                        id = 1,
                        email = "eric.norbert@proton.me"
                    )
                ),
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(
                        id = 2,
                        email = "eric.work@proton.me"
                    )
                )
            ),
            canApply = IsButtonEnabled.from(false),
            canUpgrade = false
        ),
        SelectMailboxesUiState(
            mailboxes = listOf(
                SelectedAliasMailboxUiModel(
                    selected = true,
                    model = AliasMailboxUiModel(
                        id = 1,
                        email = "eric.norbert@proton.me"
                    )
                ),
                SelectedAliasMailboxUiModel(
                    selected = true,
                    model = AliasMailboxUiModel(
                        id = 2,
                        email = "eric.work@proton.me"
                    )
                )
            ),
            canApply = IsButtonEnabled.from(true),
            canUpgrade = false
        ),
        SelectMailboxesUiState(
            mailboxes = listOf(
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(
                        id = 1,
                        email = "eric.norbert@proton.me"
                    )
                ),
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(
                        id = 2,
                        email = "eric.work@proton.me"
                    )
                )
            ),
            canApply = IsButtonEnabled.from(false),
            canUpgrade = true
        )
    )
}
