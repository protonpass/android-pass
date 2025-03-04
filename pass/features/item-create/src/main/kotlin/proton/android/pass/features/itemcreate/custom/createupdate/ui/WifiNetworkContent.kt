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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.itemcreate.common.PasswordInput
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemStaticFields

@Composable
fun WifiNetworkContent(
    modifier: Modifier = Modifier,
    itemStaticFields: ItemStaticFields.WifiNetwork,
    isEditAllowed: Boolean,
    onEvent: (ItemContentEvent) -> Unit
) {
    Column(modifier = modifier.roundedContainerNorm()) {
        SSIDInput(
            text = itemStaticFields.ssid,
            isEditAllowed = isEditAllowed,
            onChange = {
                onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.SSID, it))
            }
        )
        PassDivider()
        PasswordInput(
            value = itemStaticFields.password,
            passwordStrength = PasswordStrength.None,
            isEditAllowed = isEditAllowed,
            onChange = {
                onEvent(ItemContentEvent.OnFieldValueChange(FieldChange.Password, it))
            },
            onFocus = {
                onEvent(ItemContentEvent.OnFieldFocusChange(FieldChange.Password, it))
            }
        )
    }
}
