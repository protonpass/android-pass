/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.detail.presentation.handlers

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsSource
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.usecases.attachments.ObserveAllItemRevisionAttachments
import proton.android.pass.data.api.usecases.attachments.ObserveDetailItemAttachments
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.item.details.detail.presentation.messages.ItemDetailsSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

class ItemDetailsHandlerImpl @Inject constructor(
    private val observeShare: ObserveShare,
    private val observeDetailItemAttachments: ObserveDetailItemAttachments,
    private val observeAllItemRevisionAttachments: ObserveAllItemRevisionAttachments,
    private val observers: Map<ItemCategory, @JvmSuppressWildcards ItemDetailsHandlerObserver<*>>,
    private val clipboardManager: ClipboardManager,
    private val attachmentsHandler: AttachmentsHandler,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher
) : ItemDetailsHandler {

    override fun observeItemDetails(
        item: Item,
        source: ItemDetailsSource,
        savedStateEntries: Map<String, Any?>
    ): Flow<ItemDetailState> = combine(
        oneShot { observeShare(item.shareId).first() },
        attachmentsFlow(item, source),
        ::Pair
    )
        .flatMapLatest { (share, attachments) ->
            getItemDetailsObserver(item.itemType.category).observe(
                share = share,
                item = item,
                attachmentsState = attachments,
                savedStateEntries = savedStateEntries
            )
        }
        .catch { error ->
            if (error !is ItemNotFoundError) {
                PassLogger.w(TAG, "There was an error observing item details")
                PassLogger.w(TAG, error)
            }
        }
        .distinctUntilChanged()

    override suspend fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        attachmentsHandler.openAttachment(contextHolder, attachment)
    }

    override suspend fun onItemDetailsFieldClicked(fieldType: ItemDetailsFieldType) {
        when (fieldType) {
            is ItemDetailsFieldType.Copyable -> {
                clipboardManager.copyToClipboard(text = fieldType.text, isSecure = false)
                displayFieldCopiedSnackbarMessage(fieldType)
            }
            is ItemDetailsFieldType.Hidden -> {
                val text = when (val value = fieldType.hiddenState) {
                    is HiddenState.Empty -> ""
                    is HiddenState.Revealed -> value.clearText
                    is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                        decrypt(value.encrypted)
                    }
                }

                clipboardManager.copyToClipboard(text = text, isSecure = true)
                displayFieldCopiedSnackbarMessage(fieldType)
            }
        }

    }


    private fun attachmentsFlow(item: Item, source: ItemDetailsSource): Flow<AttachmentsState> {
        val attachmentsFlow = when (source) {
            ItemDetailsSource.DETAIL -> {
                if (item.hasAttachments) observeDetailItemAttachments(item.shareId, item.id)
                else return flowOf(AttachmentsState.Initial)
            }
            ItemDetailsSource.REVISION -> {
                if (item.hasAttachments || item.hasHadAttachments) {
                    observeAllItemRevisionAttachments(item.shareId, item.id)
                        .map { it.filter { attachment -> attachment.existsForRevision(item.revision) } }
                } else {
                    return flowOf(AttachmentsState.Initial)
                }
            }
        }

        return combine(attachmentsFlow, attachmentsHandler.attachmentState) { attachments, attachmentState ->
            attachmentState.copy(attachmentsList = attachments)
        }
    }

    private suspend fun displayFieldCopiedSnackbarMessage(fieldType: ItemDetailsFieldType) = when (fieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
        is ItemDetailsFieldType.Hidden.Cvv -> ItemDetailsSnackbarMessage.CvvCopied
        is ItemDetailsFieldType.Hidden.Password -> ItemDetailsSnackbarMessage.PasswordCopied
        is ItemDetailsFieldType.Hidden.Pin -> ItemDetailsSnackbarMessage.PinCopied
        is ItemDetailsFieldType.Hidden.PrivateKey -> ItemDetailsSnackbarMessage.PrivateKeyCopied
        is ItemDetailsFieldType.Hidden.SocialSecurityNumber -> ItemDetailsSnackbarMessage.SocialSecurityNumberCopied
        is ItemDetailsFieldType.Copyable.Alias -> ItemDetailsSnackbarMessage.AliasCopied
        is ItemDetailsFieldType.Copyable.BirthDate -> ItemDetailsSnackbarMessage.BirthDateCopied
        is ItemDetailsFieldType.Copyable.CardNumber -> ItemDetailsSnackbarMessage.CardNumberCopied
        is ItemDetailsFieldType.Copyable.City -> ItemDetailsSnackbarMessage.CityCopied
        is ItemDetailsFieldType.Copyable.Company -> ItemDetailsSnackbarMessage.CompanyCopied
        is ItemDetailsFieldType.Copyable.CountryOrRegion -> ItemDetailsSnackbarMessage.CountryOrRegionCopied
        is ItemDetailsFieldType.Copyable.County -> ItemDetailsSnackbarMessage.CountyCopied
        is ItemDetailsFieldType.Copyable.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
        is ItemDetailsFieldType.Copyable.Email -> ItemDetailsSnackbarMessage.EmailCopied
        is ItemDetailsFieldType.Copyable.Facebook -> ItemDetailsSnackbarMessage.FacebookCopied
        is ItemDetailsFieldType.Copyable.FirstName -> ItemDetailsSnackbarMessage.FirstNameCopied
        is ItemDetailsFieldType.Copyable.Floor -> ItemDetailsSnackbarMessage.FloorCopied
        is ItemDetailsFieldType.Copyable.FullName -> ItemDetailsSnackbarMessage.FullNameCopied
        is ItemDetailsFieldType.Copyable.Gender -> ItemDetailsSnackbarMessage.GenderCopied
        is ItemDetailsFieldType.Copyable.Instagram -> ItemDetailsSnackbarMessage.InstagramCopied
        is ItemDetailsFieldType.Copyable.LastName -> ItemDetailsSnackbarMessage.LastNameCopied
        is ItemDetailsFieldType.Copyable.LicenseNumber -> ItemDetailsSnackbarMessage.LicenseNumberCopied
        is ItemDetailsFieldType.Copyable.LinkedIn -> ItemDetailsSnackbarMessage.LinkedInCopied
        is ItemDetailsFieldType.Copyable.MiddleName -> ItemDetailsSnackbarMessage.MiddleNameCopied
        is ItemDetailsFieldType.Copyable.Occupation -> ItemDetailsSnackbarMessage.OccupationCopied
        is ItemDetailsFieldType.Copyable.Organization -> ItemDetailsSnackbarMessage.OrganizationCopied
        is ItemDetailsFieldType.Copyable.PassportNumber -> ItemDetailsSnackbarMessage.PassportNumberCopied
        is ItemDetailsFieldType.Copyable.PhoneNumber -> ItemDetailsSnackbarMessage.PhoneNumberCopied
        is ItemDetailsFieldType.Copyable.Reddit -> ItemDetailsSnackbarMessage.RedditCopied
        is ItemDetailsFieldType.Copyable.StateOrProvince -> ItemDetailsSnackbarMessage.StateOrProvinceCopied
        is ItemDetailsFieldType.Copyable.StreetAddress -> ItemDetailsSnackbarMessage.StreetAddressCopied
        is ItemDetailsFieldType.Copyable.TotpCode -> ItemDetailsSnackbarMessage.TotpCodeCopied
        is ItemDetailsFieldType.Copyable.Username -> ItemDetailsSnackbarMessage.UsernameCopied
        is ItemDetailsFieldType.Copyable.Website -> ItemDetailsSnackbarMessage.WebsiteCopied
        is ItemDetailsFieldType.Copyable.XHandle -> ItemDetailsSnackbarMessage.XHandleCopied
        is ItemDetailsFieldType.Copyable.Yahoo -> ItemDetailsSnackbarMessage.YahooCopied
        is ItemDetailsFieldType.Copyable.ZipOrPostalCode -> ItemDetailsSnackbarMessage.ZipOrPostalCodeCopied
        is ItemDetailsFieldType.Copyable.PublicKey -> ItemDetailsSnackbarMessage.PublicKeyCopied
        is ItemDetailsFieldType.Copyable.SSID -> ItemDetailsSnackbarMessage.SSIDCopied
    }.let { snackbarMessage -> snackbarDispatcher(snackbarMessage) }

    override fun updateItemDetailsContent(
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.Hidden>>,
        itemCategory: ItemCategory,
        itemContents: ItemContents
    ): ItemContents = getItemDetailsObserver(itemCategory).updateHiddenFieldsContents(
        itemContents = itemContents,
        revealedHiddenFields = revealedHiddenFields
    )

    override fun updateItemDetailsDiffs(
        itemCategory: ItemCategory,
        baseItemContents: ItemContents,
        otherItemContents: ItemContents,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = getItemDetailsObserver(itemCategory)
        .calculateItemDiffs(
            baseItemContents = baseItemContents,
            otherItemContents = otherItemContents,
            baseAttachments = baseAttachments,
            otherAttachments = otherAttachments
        )

    @Suppress("UNCHECKED_CAST")
    private fun getItemDetailsObserver(itemCategory: ItemCategory): ItemDetailsHandlerObserver<ItemContents> =
        observers[itemCategory] as? ItemDetailsHandlerObserver<ItemContents>
            ?: throw IllegalStateException("Unsupported item category: $itemCategory")

    private companion object {

        private const val TAG = "ItemDetailsHandlerImpl"

    }

}
