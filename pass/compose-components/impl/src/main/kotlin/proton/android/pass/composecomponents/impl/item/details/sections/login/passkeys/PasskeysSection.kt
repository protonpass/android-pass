/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffs

@Composable
fun PasskeysSection(
    modifier: Modifier = Modifier,
    passkeys: ImmutableList<UIPasskeyContent>,
    onSelected: (UIPasskeyContent) -> Unit,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Login = ItemDiffs.Login()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        passkeys.forEach { passkey ->
            PasskeyRow(
                domain = passkey.domain,
                username = passkey.userName,
                itemDiffType = itemDiffs.passkey(passkeyId = passkey.id),
                itemColors = itemColors,
                onClick = { onSelected(passkey) }
            )
        }
    }
}

