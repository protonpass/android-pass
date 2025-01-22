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

package proton.android.pass.features.itemdetail.alias

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.domain.AliasMailbox

@Composable
fun AliasSection(
    modifier: Modifier = Modifier,
    alias: String,
    isAliasEnabled: Boolean,
    isAliasCreatedByUser: Boolean,
    isAliasSyncEnabled: Boolean,
    isAliasStateToggling: Boolean,
    mailboxes: PersistentList<AliasMailbox>,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit,
    onCreateLoginFromAlias: (String) -> Unit,
    onToggleAliasState: (Boolean) -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        AliasAddressRow(
            alias = alias,
            isAliasEnabled = isAliasEnabled,
            isAliasCreatedByUser = isAliasCreatedByUser,
            isAliasSyncEnabled = isAliasSyncEnabled,
            isAliasStateToggling = isAliasStateToggling,
            onCopyAlias = { onCopyAlias(it) },
            onCreateLoginFromAlias = onCreateLoginFromAlias,
            onToggleAliasState = onToggleAliasState
        )
        if (!mailboxes.isEmpty() || isLoading) {
            Divider(color = PassTheme.colors.inputBorderNorm)
        }
        AliasMailboxesRow(
            mailboxes = mailboxes,
            isLoading = isLoading
        )
    }
}

internal class ThemedAliasMailboxesPreviewProvider :
    ThemePairPreviewProvider<List<AliasMailbox>>(AliasMailboxesPreviewProvider())

@[Preview Composable]
internal fun AliasSectionPreview(
    @PreviewParameter(ThemedAliasMailboxesPreviewProvider::class) input: Pair<Boolean, List<AliasMailbox>>
) {
    val (isDark, mailboxes) = input

    PassTheme(isDark = isDark) {
        Surface {
            AliasSection(
                alias = "myalias@myalias.com",
                isAliasEnabled = false,
                isAliasCreatedByUser = false,
                isAliasSyncEnabled = false,
                isAliasStateToggling = false,
                mailboxes = mailboxes.toPersistentList(),
                isLoading = false,
                onCopyAlias = {},
                onCreateLoginFromAlias = {},
                onToggleAliasState = {}
            )
        }
    }
}
