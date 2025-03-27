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
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.WorkDetailsContent

class IdentityRowPreviewProvider : PreviewParameterProvider<IdentityRowParameter> {
    override val values: Sequence<IdentityRowParameter>
        get() = sequenceOf(
            with(title = "Empty identity"),
            with(title = "Empty identity", highlight = "Doe"),
            with(title = "Empty identity", highlight = "John")
        )

    companion object {
        private fun with(title: String, highlight: String = ""): IdentityRowParameter = IdentityRowParameter(
            model = ItemUiModel(
                id = ItemId("123"),
                userId = UserId("user-id"),
                shareId = ShareId("345"),
                contents = ItemContents.Identity(
                    title = title,
                    note = "",
                    personalDetailsContent = PersonalDetailsContent(
                        fullName = "John Doe",
                        firstName = "John",
                        lastName = "",
                        middleName = "",
                        email = "john.doe@proton.me",
                        birthdate = "",
                        gender = "",
                        phoneNumber = "",
                        customFields = emptyList()
                    ),
                    addressDetailsContent = AddressDetailsContent.EMPTY,
                    contactDetailsContent = ContactDetailsContent.EMPTY,
                    workDetailsContent = WorkDetailsContent.EMPTY,
                    extraSectionContentList = emptyList()
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
            highlight = highlight
        )
    }
}

data class IdentityRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

