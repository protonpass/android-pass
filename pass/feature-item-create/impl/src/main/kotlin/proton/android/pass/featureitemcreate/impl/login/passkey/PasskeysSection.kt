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

package proton.android.pass.featureitemcreate.impl.login.passkey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.featureitemcreate.impl.login.LoginContentEvent

@Composable
fun PasskeysSection(
    modifier: Modifier = Modifier,
    passkeys: ImmutableList<UIPasskeyContent>,
    onEvent: (LoginContentEvent) -> Unit
) {
    if (passkeys.isEmpty()) return

    Column(modifier = modifier) {
        passkeys.forEachIndexed { idx, passkey ->
            PasskeyEditRow(
                modifier = Modifier.padding(vertical = Spacing.small),
                domain = passkey.domain,
                username = passkey.userName,
                canDelete = true,
                onDeleteClick = { onEvent(LoginContentEvent.OnDeletePasskey(idx, passkey)) }
            )
        }
    }
}
