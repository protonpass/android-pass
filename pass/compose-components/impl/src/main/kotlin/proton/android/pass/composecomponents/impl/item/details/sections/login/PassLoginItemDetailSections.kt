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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.login.passkeys.PasskeysSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Totp
import proton.android.pass.domain.items.ItemCustomField

@Composable
internal fun PassLoginItemDetailSections(
    modifier: Modifier = Modifier,
    contents: ItemContents.Login,
    passwordStrength: PasswordStrength,
    primaryTotp: Totp?,
    customFields: ImmutableList<ItemCustomField>,
    passkeys: ImmutableList<UIPasskeyContent>,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(contents) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (passkeys.isNotEmpty()) {
            PasskeysSection(
                passkeys = passkeys,
                onSelected = {
                    onEvent(PassItemDetailsUiEvent.OnPasskeyClick(it))
                }
            )
        }

        PassLoginItemDetailMainSection(
            username = itemEmail,
            password = password,
            passwordStrength = passwordStrength,
            primaryTotp = primaryTotp,
            itemColors = itemColors,
            onEvent = onEvent
        )

        if (urls.isNotEmpty()) {
            PassLoginItemDetailWebsitesSection(
                websiteUrls = urls.toPersistentList(),
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (note.isNotBlank()) {
            PassSharedItemDetailNoteSection(
                note = note,
                itemColors = itemColors
            )
        }

        if (customFields.isNotEmpty()) {
            PassLoginItemDetailCustomFieldsSection(
                customFields = customFields,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (packageInfoSet.isNotEmpty()) {
            val mapped = remember(packageInfoSet.hashCode()) {
                packageInfoSet.map { PackageInfoUi(it) }.toPersistentSet()
            }
            LinkedAppsListSection(
                packageInfoUiSet = mapped,
                isEditable = false,
                onLinkedAppDelete = {}
            )
        }
    }
}
