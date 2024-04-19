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

package proton.android.pass.composecomponents.impl.pinning

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class PinItemPreviewProvider : PreviewParameterProvider<ItemUiModel> {
    override val values: Sequence<ItemUiModel>
        get() = sequenceOf(
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                contents = ItemContents.Note(
                    "Item with long text and a maximum",
                    ""
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = true
            ),
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                contents = ItemContents.Login(
                    title = "Login title",
                    note = "",
                    username = "",
                    password = HiddenState.Empty(""),
                    urls = listOf(),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Empty(""),
                    customFields = listOf(),
                    passkeys = emptyList()
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = true
            ),
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                contents = ItemContents.Alias(
                    title = "Alias title",
                    note = "",
                    aliasEmail = ""
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = true
            ),
            ItemUiModel(
                id = ItemId("123"),
                shareId = ShareId("345"),
                contents = ItemContents.CreditCard(
                    title = "Credit card title",
                    note = "",
                    cardHolder = "",
                    type = CreditCardType.MasterCard,
                    number = "",
                    cvv = HiddenState.Empty(""),
                    pin = HiddenState.Empty(""),
                    expirationDate = ""
                ),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = true
            )
        )
}
