/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemdetail.impl.common.HistorySection
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldDetails

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    passwordScore: PasswordScore?,
    vault: Vault?,
    totpUiState: TotpUiState?,
    moreInfoUiState: MoreInfoUiState,
    showViewAlias: Boolean,
    canLoadExternalImages: Boolean,
    customFields: ImmutableList<CustomFieldUiContent>,
    onEvent: (LoginDetailEvent) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Login

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoginTitle(
            modifier = Modifier.padding(0.dp, 12.dp),
            title = itemUiModel.contents.title,
            vault = vault,
            website = contents.urls.firstOrNull(),
            packageName = contents.packageInfoSet.minByOrNull { it.packageName.value }?.packageName?.value,
            canLoadExternalImages = canLoadExternalImages,
            onVaultClick = { onEvent(LoginDetailEvent.OnVaultClick) },
            isPinned = itemUiModel.isPinned,
        )

        MainLoginSection(
            username = contents.username,
            passwordHiddenState = contents.password,
            passwordScore = passwordScore,
            totpUiState = totpUiState,
            showViewAlias = showViewAlias,
            onEvent = onEvent
        )

        WebsiteSection(
            websites = contents.urls.toPersistentList(),
            onEvent = onEvent
        )

        NoteSection(
            text = itemUiModel.contents.note,
            accentColor = PassTheme.colors.loginInteractionNorm
        )

        HistorySection(
            createdInstant = itemUiModel.createTime,
            modifiedInstant = itemUiModel.modificationTime,
            onViewItemHistoryClicked = {},
            buttonBackgroundColor = PassTheme.colors.loginInteractionNormMinor2,
            buttonTextColor = PassTheme.colors.loginInteractionNormMajor2,
        )

        CustomFieldDetails(
            fields = customFields,
            onEvent = { onEvent(LoginDetailEvent.OnCustomFieldEvent(it)) }
        )

        LinkedAppsListSection(
            packageInfoUiSet = contents.packageInfoSet.map { PackageInfoUi(it) }.toPersistentSet(),
            isEditable = false,
            onLinkedAppDelete = {}
        )

        MoreInfo(moreInfoUiState = moreInfoUiState)
    }

}
