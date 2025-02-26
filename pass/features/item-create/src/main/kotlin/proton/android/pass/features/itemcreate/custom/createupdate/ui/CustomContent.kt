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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.features.itemcreate.common.CreateUpdateTopBar
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemSharedProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomContent(
    modifier: Modifier = Modifier,
    itemFormState: ItemFormState,
    topBarActionName: String,
    itemSharedProperties: ItemSharedProperties,
    canUseAttachments: Boolean,
    onEvent: (ItemContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = itemSharedProperties.isLoading,
                actionColor = PassTheme.colors.interactionNormMajor1,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                selectedVault = itemSharedProperties.selectedVault.value(),
                showVaultSelector = itemSharedProperties.shouldShowVaultSelector,
                onCloseClick = { onEvent(ItemContentEvent.Up) },
                onActionClick = {
                    itemSharedProperties.selectedVault.value()?.shareId?.let {
                        onEvent(ItemContentEvent.Submit(it))
                    }
                },
                onUpgrade = { },
                onVaultSelectorClick = {
                    itemSharedProperties.selectedVault.value()?.shareId?.let {
                        onEvent(ItemContentEvent.Submit(it))
                    }
                }
            )
        }
    ) { padding ->
        ItemForm(
            modifier = Modifier.padding(padding),
            itemFormState = itemFormState,
            itemSharedProperties = itemSharedProperties,
            canUseAttachments = canUseAttachments,
            onEvent = onEvent
        )
    }
}
