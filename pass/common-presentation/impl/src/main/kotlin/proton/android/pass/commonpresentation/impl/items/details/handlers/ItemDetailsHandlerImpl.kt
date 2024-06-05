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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonpresentation.impl.items.details.messages.ItemDetailsSnackbarMessage
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

class ItemDetailsHandlerImpl @Inject constructor(
    private val observers: Map<ItemCategory, @JvmSuppressWildcards ItemDetailsHandlerObserver>,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher
) : ItemDetailsHandler {

    override fun observeItemDetails(item: Item): Flow<ItemDetailState> = getItemDetailsObserver(item.itemType.category)
        .observe(item)
        .distinctUntilChanged()

    override suspend fun onItemDetailsFieldClicked(text: String, plainFieldType: ItemDetailsFieldType.Plain) {
        clipboardManager.copyToClipboard(text = text, isSecure = false)
        displayFieldCopiedSnackbarMessage(plainFieldType)
    }

    override suspend fun onItemDetailsHiddenFieldClicked(
        hiddenState: HiddenState,
        hiddenFieldType: ItemDetailsFieldType.Hidden
    ) {
        val text = when (hiddenState) {
            is HiddenState.Empty -> ""
            is HiddenState.Revealed -> hiddenState.clearText
            is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                decrypt(hiddenState.encrypted)
            }
        }

        clipboardManager.copyToClipboard(text = text, isSecure = true)
        displayFieldCopiedSnackbarMessage(hiddenFieldType)
    }

    private suspend fun displayFieldCopiedSnackbarMessage(fieldType: ItemDetailsFieldType) = when (fieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
        ItemDetailsFieldType.Hidden.Cvv -> ItemDetailsSnackbarMessage.CvvCopied
        ItemDetailsFieldType.Hidden.Password -> ItemDetailsSnackbarMessage.PasswordCopied
        ItemDetailsFieldType.Hidden.Pin -> ItemDetailsSnackbarMessage.PinCopied
        ItemDetailsFieldType.Plain.Alias -> ItemDetailsSnackbarMessage.AliasCopied
        ItemDetailsFieldType.Plain.BirthDate -> ItemDetailsSnackbarMessage.BirthDate
        ItemDetailsFieldType.Plain.CardNumber -> ItemDetailsSnackbarMessage.CardNumberCopied
        ItemDetailsFieldType.Plain.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
        ItemDetailsFieldType.Plain.Email -> ItemDetailsSnackbarMessage.EmailCopied
        ItemDetailsFieldType.Plain.FirstName -> ItemDetailsSnackbarMessage.FirstName
        ItemDetailsFieldType.Plain.FullName -> ItemDetailsSnackbarMessage.FullName
        ItemDetailsFieldType.Plain.Gender -> ItemDetailsSnackbarMessage.Gender
        ItemDetailsFieldType.Plain.LastName -> ItemDetailsSnackbarMessage.LastName
        ItemDetailsFieldType.Plain.MiddleName -> ItemDetailsSnackbarMessage.MiddleName
        ItemDetailsFieldType.Plain.PhoneNumber -> ItemDetailsSnackbarMessage.PhoneNumber
        ItemDetailsFieldType.Plain.TotpCode -> ItemDetailsSnackbarMessage.TotpCodeCopied
        ItemDetailsFieldType.Plain.Username -> ItemDetailsSnackbarMessage.UsernameCopied
        ItemDetailsFieldType.Plain.Website -> ItemDetailsSnackbarMessage.WebsiteCopied
    }.let { snackbarMessage -> snackbarDispatcher(snackbarMessage) }

    override fun onItemDetailsHiddenFieldToggled(
        isVisible: Boolean,
        hiddenState: HiddenState,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        itemCategory: ItemCategory
    ) {
        encryptionContextProvider.withEncryptionContext {
            when {
                isVisible -> HiddenState.Revealed(
                    encrypted = hiddenState.encrypted,
                    clearText = decrypt(hiddenState.encrypted)
                )

                decrypt(hiddenState.encrypted.toEncryptedByteArray()).isEmpty() -> HiddenState.Empty(
                    encrypt("")
                )

                else -> HiddenState.Concealed(encrypted = hiddenState.encrypted)
            }
        }.let { toggledHiddenState ->
            getItemDetailsObserver(itemCategory)
                .updateHiddenState(hiddenFieldType, toggledHiddenState)
        }
    }

    private fun getItemDetailsObserver(itemCategory: ItemCategory) = observers[itemCategory]
        ?: throw IllegalStateException("Unsupported item category: $itemCategory")

}
