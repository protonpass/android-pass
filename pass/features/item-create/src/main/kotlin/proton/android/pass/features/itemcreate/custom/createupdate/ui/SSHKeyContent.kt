/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.SshKeyType
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemStaticFields
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SSHKeyContent(
    modifier: Modifier = Modifier,
    itemStaticFields: ItemStaticFields.SSHKey,
    isEditAllowed: Boolean,
    isGenerating: Boolean,
    onEvent: (ItemContentEvent) -> Unit
) {
    Column(modifier = modifier.roundedContainerNorm()) {
        if (isEditAllowed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                horizontalArrangement = Arrangement.End
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    TextButton(
                        onClick = {
                            onEvent(ItemContentEvent.OnOpenSshKeyType(SshKeyType.ED25519))
                        }
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_key),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(Spacing.small))
                        Text(stringResource(R.string.ssh_key_generate_button))
                    }
                }
            }
            PassDivider()
        }

        PublicKeyInput(
            text = itemStaticFields.publicKey,
            isEditAllowed = isEditAllowed,
            onChange = {
                onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.PublicKey, it))
            }
        )

        PassDivider()

        PrivateKeyInput(
            content = itemStaticFields.privateKey,
            isEditAllowed = isEditAllowed,
            onChange = {
                onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.PrivateKey, it))
            },
            onFocusChange = {
                onEvent(ItemContentEvent.OnFieldFocusChange(FieldChange.PrivateKey, it))
            }
        )
    }
}
