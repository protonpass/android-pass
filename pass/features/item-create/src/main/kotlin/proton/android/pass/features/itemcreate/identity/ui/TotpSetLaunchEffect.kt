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

package proton.android.pass.features.itemcreate.identity.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import proton.android.pass.common.api.Some
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField

@Composable
fun TotpSetLaunchEffect(totpNavParams: TotpNavParams?, callback: (IdentityField, String) -> Unit) {
    LaunchedEffect(totpNavParams) {
        val section = if (totpNavParams?.specialSectionIndex is Some) {
            IdentitySectionType.from(
                index = totpNavParams.specialSectionIndex.value,
                sectionIndex = totpNavParams.sectionIndex
            )
        } else {
            return@LaunchedEffect
        }
        val field = IdentityField.CustomField(
            sectionType = section,
            customFieldType = CustomFieldType.Totp,
            index = totpNavParams.index.value() ?: return@LaunchedEffect
        )
        callback(field, totpNavParams.uri)
    }
}
