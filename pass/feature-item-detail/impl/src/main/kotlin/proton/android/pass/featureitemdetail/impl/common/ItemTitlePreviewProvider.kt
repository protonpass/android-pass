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
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import java.util.Date

class ThemeItemTitleProvider : ThemePairPreviewProvider<ItemTitleInput>(ItemTitlePreviewProvider())

class ItemTitlePreviewProvider : PreviewParameterProvider<ItemTitleInput> {
    override val values: Sequence<ItemTitleInput>
        get() = sequence {
            yield(
                ItemTitleInput(
                    vault = null,
                    isPinned = false
                )
            )
            yield(
                ItemTitleInput(
                    vault = Vault(
                        userId = UserId(id = ""),
                        shareId = ShareId("123"),
                        vaultId = VaultId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        createTime = Date()
                    ),
                    isPinned = false
                )
            )
            yield(
                ItemTitleInput(
                    vault = null,
                    isPinned = true
                )
            )
            yield(
                ItemTitleInput(
                    vault = Vault(
                        userId = UserId(id = ""),
                        shareId = ShareId("123"),
                        vaultId = VaultId("123"),
                        name = "A vault",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                        createTime = Date()
                    ),
                    isPinned = true
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
        revision = 1
    ),
    val vault: Vault?,
    val isPinned: Boolean
)
