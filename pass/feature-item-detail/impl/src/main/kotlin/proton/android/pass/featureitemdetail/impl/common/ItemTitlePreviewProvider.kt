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

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultId
import java.util.Date

class ThemeItemTitleProvider : ThemePairPreviewProvider<ItemTitleInput>(ItemTitlePreviewProvider())

class ItemTitlePreviewProvider : PreviewParameterProvider<ItemTitleInput> {
    override val values: Sequence<ItemTitleInput>
        get() = sequence {
            yield(
                ItemTitleInput(
                    share = Share.Item(
                        userId = UserId(id = ""),
                        id = ShareId("123"),
                        vaultId = VaultId("123"),
                        createTime = Date(),
                        targetId = "target-id",
                        permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                        expirationTime = null,
                        shareRole = ShareRole.Admin,
                        isOwner = true,
                        memberCount = 1,
                        shared = false,
                        maxMembers = 11,
                        pendingInvites = 0,
                        newUserInvitesReady = 0,
                        canAutofill = true
                    ),
                    isPinned = false,
                    isHistoryFeatureEnabled = false
                )
            )
            yield(
                ItemTitleInput(
                    share = Share.Vault(
                        userId = UserId(id = ""),
                        id = ShareId("123"),
                        vaultId = VaultId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        createTime = Date(),
                        targetId = "target-id",
                        permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                        expirationTime = null,
                        shareRole = ShareRole.Admin,
                        isOwner = true,
                        memberCount = 1,
                        shared = false,
                        maxMembers = 11,
                        pendingInvites = 0,
                        newUserInvitesReady = 0,
                        canAutofill = true
                    ),
                    isPinned = false,
                    isHistoryFeatureEnabled = false
                )
            )
            yield(
                ItemTitleInput(
                    share = Share.Item(
                        userId = UserId(id = ""),
                        id = ShareId("123"),
                        vaultId = VaultId("123"),
                        createTime = Date(),
                        targetId = "target-id",
                        permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                        expirationTime = null,
                        shareRole = ShareRole.Admin,
                        isOwner = true,
                        memberCount = 1,
                        shared = false,
                        maxMembers = 11,
                        pendingInvites = 0,
                        newUserInvitesReady = 0,
                        canAutofill = true
                    ),
                    isPinned = true,
                    isHistoryFeatureEnabled = false
                )
            )
            yield(
                ItemTitleInput(
                    share = Share.Vault(
                        userId = UserId(id = ""),
                        id = ShareId("123"),
                        vaultId = VaultId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        createTime = Date(),
                        targetId = "target-id",
                        permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                        expirationTime = null,
                        shareRole = ShareRole.Admin,
                        isOwner = true,
                        memberCount = 1,
                        shared = false,
                        maxMembers = 11,
                        pendingInvites = 0,
                        newUserInvitesReady = 0,
                        canAutofill = true
                    ),
                    isPinned = true,
                    isHistoryFeatureEnabled = false
                )
            )
        }
}

@Suppress("MagicNumber")
data class ItemTitleInput(
    val itemUiModel: ItemUiModel = ItemUiModel(
        id = ItemId("123"),
        userId = UserId("user-id"),
        shareId = ShareId("123"),
        contents = ItemContents.Note(
            title = "A really long title to check if the element is multiline",
            note = "Note body"
        ),
        state = 0,
        createTime = Instant.fromEpochMilliseconds(1_697_213_366_026),
        modificationTime = Instant.fromEpochMilliseconds(1_707_213_366_026),
        lastAutofillTime = null,
        isPinned = false,
        revision = 1,
        shareCount = 0
    ),
    val share: Share,
    val isPinned: Boolean,
    val isHistoryFeatureEnabled: Boolean
)
