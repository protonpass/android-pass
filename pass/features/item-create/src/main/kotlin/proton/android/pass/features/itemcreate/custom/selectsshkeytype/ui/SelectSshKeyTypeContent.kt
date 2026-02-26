/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.features.itemcreate.custom.selectsshkeytype.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.SshKeyType
import proton.android.pass.features.itemcreate.R

@Composable
fun SelectSshKeyTypeContent(modifier: Modifier = Modifier, onSelect: (SshKeyType) -> Unit) {
    BottomSheetItemList(
        modifier = modifier,
        items = SshKeyType.entries.map {
            sshKeyTypeRow(it, onSelect)
        }.withDividers().toPersistentList()
    )
}

private fun sshKeyTypeRow(sshKeyType: SshKeyType, onClick: (SshKeyType) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(text = getSshKeyTypeText(sshKeyType))
            }
        override val subtitle: (@Composable () -> Unit)? = null
        override val leftIcon: (@Composable () -> Unit)? = null
        override val endIcon: @Composable (() -> Unit)? = null
        override val onClick: () -> Unit = { onClick(sshKeyType) }
        override val isDivider = false
    }

@Composable
private fun getSshKeyTypeText(type: SshKeyType): String = when (type) {
    SshKeyType.ED25519 -> stringResource(R.string.ssh_key_type_ed25519)
    SshKeyType.RSA_2048 -> stringResource(R.string.ssh_key_type_rsa_2048)
    SshKeyType.RSA_4096 -> stringResource(R.string.ssh_key_type_rsa_4096)
}
