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

package proton.android.pass.featureitemdetail.impl.login.reusedpass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.item.LoginRow
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.login.reusedpass.presentation.LoginItemDetailReusedPassState

@Composable
internal fun LoginItemDetailReusedPassContent(
    modifier: Modifier = Modifier,
    onNavigated: (ItemDetailNavigation) -> Unit,
    state: LoginItemDetailReusedPassState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                onUpClick = { onNavigated(ItemDetailNavigation.Back) }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .background(PassTheme.colors.backgroundStrong)
                .padding(paddingValues = innerPaddingValues)
                .padding(top = Spacing.large)
        ) {
            LoginItemDetailReusedPassHeader(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                password = password
            )

            LazyColumn(
                modifier = Modifier
                    .background(PassTheme.colors.backgroundStrong)
                    .padding(
                        start = Spacing.medium,
                        top = Spacing.large,
                        end = Spacing.medium
                    ),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                items(
                    items = duplicatedPasswordLoginItems,
                    key = { itemUiModel -> itemUiModel.id.id }
                ) { itemUiModel ->
                    LoginRow(
                        item = itemUiModel,
                        canLoadExternalImages = canLoadExternalImages,
                        vaultIcon = getShareIcon(itemUiModel.shareId)?.toSmallResource(),
                    )
                }
            }
        }
    }

}
