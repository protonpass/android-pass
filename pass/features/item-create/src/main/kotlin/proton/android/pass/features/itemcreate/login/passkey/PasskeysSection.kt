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

package proton.android.pass.features.itemcreate.login.passkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.features.itemcreate.login.LoginContentEvent

@Composable
internal fun PasskeysSection(
    modifier: Modifier = Modifier,
    passkeys: ImmutableList<UIPasskeyContent>,
    onEvent: (LoginContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        passkeys.forEachIndexed { idx, passkey ->
            PasskeyEditRow(
                domain = passkey.domain,
                username = passkey.userName,
                canDelete = true,
                onDeleteClick = { onEvent(LoginContentEvent.OnDeletePasskey(idx, passkey)) }
            )
        }
    }
}
