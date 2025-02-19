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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class CustomRowPreviewProvider : PreviewParameterProvider<CustomRowParameter> {
    override val values: Sequence<CustomRowParameter>
        get() = sequenceOf(
            with(title = "Empty Custom Row")
        )

    companion object {
        private fun with(title: String, highlight: String = ""): CustomRowParameter = CustomRowParameter(
            model = ItemUiModel(
                id = ItemId("123"),
                userId = UserId("user-id"),
                shareId = ShareId("345"),
                contents = ItemContents.Custom(
                    title = title,
                    note = "",
                    sectionContentList = emptyList()
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = false,
                revision = 1,
                shareCount = 0,
                isOwner = true
            ),
            highlight = highlight
        )
    }
}

data class CustomRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

