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

package proton.android.pass.features.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.viewHistory
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.features.home.R

@ExperimentalMaterialApi
@Composable
internal fun LoginOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    isFreePlan: Boolean,
    canUpdate: Boolean,
    canViewHistory: Boolean,
    action: BottomSheetItemAction,
    isRecentSearch: Boolean = false,
    canLoadExternalImages: Boolean,
    onCopyEmail: (String) -> Unit,
    onCopyUsername: (String) -> Unit,
    onCopyPassword: (EncryptedString) -> Unit,
    onPinned: (ShareId, ItemId) -> Unit,
    onUnpinned: (ShareId, ItemId) -> Unit,
    onViewHistory: (ShareId, ItemId) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Login

    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = contents.title) },
            subtitle = {
                BottomSheetItemSubtitle(text = contents.displayValue)
                    .takeIf { contents.displayValue.isNotEmpty() }
            },
            leftIcon = {
                val sortedPackages = contents.packageInfoSet.sortedBy { it.packageName.value }
                val packageName = sortedPackages.firstOrNull()?.packageName?.value
                val website = contents.urls.firstOrNull()
                LoginIcon(
                    text = contents.title,
                    canLoadExternalImages = canLoadExternalImages,
                    website = website,
                    packageName = packageName
                )
            }
        )

        buildList {
            if (contents.itemEmail.isNotEmpty()) {
                add(copyEmail(contents.itemEmail, onCopyEmail))
            }
            if (contents.itemUsername.isNotEmpty()) {
                add(copyUsername(contents.itemUsername, onCopyUsername))
            }
            if (contents.password !is HiddenState.Empty) {
                add(copyPassword(contents.password.encrypted, onCopyPassword))
            }

            if (itemUiModel.isPinned) {
                add(unpin(action) { onUnpinned(itemUiModel.shareId, itemUiModel.id) })
            } else {
                add(pin(action) { onPinned(itemUiModel.shareId, itemUiModel.id) })
            }

            if (canViewHistory) {
                add(viewHistory(isFreePlan) { onViewHistory(itemUiModel.shareId, itemUiModel.id) })
            }

            if (canUpdate) {
                add(edit(itemUiModel, onEdit))
                add(moveToTrash(itemUiModel, onMoveToTrash))
            }

            if (isRecentSearch) {
                add(removeFromRecentSearch(itemUiModel, onRemoveFromRecentSearch))
            }
        }.also { bottomSheetItems ->
            BottomSheetItemList(
                items = bottomSheetItems.withDividers().toPersistentList()
            )
        }
    }
}

private fun copyEmail(email: String, onCopyEmail: (String) -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_email)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onCopyEmail(email) }
    override val isDivider = false
}

private fun copyUsername(username: String, onCopyUsername: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_username)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCopyUsername(username) }
        override val isDivider = false
    }

private fun copyPassword(password: EncryptedString, onCopyPassword: (EncryptedString) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_password)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCopyPassword(password) }
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
internal fun LoginOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    userId = UserId(id = "user-id"),
                    shareId = ShareId(id = ""),
                    contents = ItemContents.Login(
                        title = "My Login",
                        note = "Note content",
                        itemEmail = "",
                        itemUsername = "My username",
                        password = HiddenState.Revealed("", "My password"),
                        urls = emptyList(),
                        packageInfoSet = emptySet(),
                        primaryTotp = HiddenState.Revealed("", ""),
                        customFields = emptyList(),
                        passkeys = emptyList()
                    ),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now(),
                    isPinned = false,
                    revision = 1,
                    shareCount = 0,
                    shareType = ShareType.Vault
                ),
                action = BottomSheetItemAction.None,
                isRecentSearch = input.second,
                canLoadExternalImages = false,
                onCopyEmail = {},
                onCopyUsername = {},
                onCopyPassword = {},
                onPinned = { _, _ -> },
                onUnpinned = { _, _ -> },
                onViewHistory = { _, _ -> },
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> },
                isFreePlan = input.second,
                canUpdate = true,
                canViewHistory = true
            )
        }
    }
}
