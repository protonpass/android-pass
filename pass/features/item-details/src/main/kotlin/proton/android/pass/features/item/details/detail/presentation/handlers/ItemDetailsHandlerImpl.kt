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
import kotlinx.coroutines.flow.MutableStateFlow
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
import proton.android.pass.commonuimodels.api.items.DetailEvent
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
    private val observers: Map<ItemCategory, @JvmSuppressWildcards ItemDetailsHandlerObserver<*, *>>,
    private val clipboardManager: ClipboardManager,
    private val attachmentsHandler: AttachmentsHandler,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher
) : ItemDetailsHandler {

    private val detailEventFlow: MutableStateFlow<DetailEvent> =
        MutableStateFlow(DetailEvent.Idle)

    override fun observeItemDetails(
        item: Item,
        source: ItemDetailsSource,
        savedStateEntries: Map<String, Any?>
    ): Flow<ItemDetailState> = combine(
        oneShot { observeShare(item.shareId).first() },
        attachmentsFlow(item, source),
        detailEventFlow,
        ::Triple
    )
        .flatMapLatest { (share, attachments, detailEvent) ->
            getItemDetailsObserver(item.itemType.category).observe(
                share = share,
                item = item,
                attachmentsState = attachments,
                savedStateEntries = savedStateEntries,
                detailEvent = detailEvent
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
            is ItemDetailsFieldType.PlainCopyable -> {
                clipboardManager.copyToClipboard(text = fieldType.text, isSecure = false)
                displayFieldCopiedSnackbarMessage(fieldType)
            }

            is ItemDetailsFieldType.HiddenCopyable -> {
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

            is ItemDetailsFieldType.AliasItemAction -> getItemDetailsObserver(ItemCategory.Alias)
                .performAction(fieldType) { detailEventFlow.emit(it) }
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

        return combine(
            attachmentsFlow,
            attachmentsHandler.attachmentState
        ) { attachments, attachmentState ->
            attachmentState.copy(attachmentsList = attachments)
        }
    }

    private suspend fun displayFieldCopiedSnackbarMessage(fieldType: ItemDetailsFieldType) {
        val snackbarMessage = when (fieldType) {
            is ItemDetailsFieldType.HiddenCopyable.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
            is ItemDetailsFieldType.HiddenCopyable.Cvv -> ItemDetailsSnackbarMessage.CvvCopied
            is ItemDetailsFieldType.HiddenCopyable.Password -> ItemDetailsSnackbarMessage.PasswordCopied
            is ItemDetailsFieldType.HiddenCopyable.Pin -> ItemDetailsSnackbarMessage.PinCopied
            is ItemDetailsFieldType.HiddenCopyable.PrivateKey -> ItemDetailsSnackbarMessage.PrivateKeyCopied
            is ItemDetailsFieldType.HiddenCopyable.SocialSecurityNumber ->
                ItemDetailsSnackbarMessage.SocialSecurityNumberCopied
            is ItemDetailsFieldType.PlainCopyable.Alias -> ItemDetailsSnackbarMessage.AliasCopied
            is ItemDetailsFieldType.PlainCopyable.BirthDate -> ItemDetailsSnackbarMessage.BirthDateCopied
            is ItemDetailsFieldType.PlainCopyable.CardNumber -> ItemDetailsSnackbarMessage.CardNumberCopied
            is ItemDetailsFieldType.PlainCopyable.City -> ItemDetailsSnackbarMessage.CityCopied
            is ItemDetailsFieldType.PlainCopyable.Company -> ItemDetailsSnackbarMessage.CompanyCopied
            is ItemDetailsFieldType.PlainCopyable.CountryOrRegion -> ItemDetailsSnackbarMessage.CountryOrRegionCopied
            is ItemDetailsFieldType.PlainCopyable.County -> ItemDetailsSnackbarMessage.CountyCopied
            is ItemDetailsFieldType.PlainCopyable.CustomField -> ItemDetailsSnackbarMessage.CustomFieldCopied
            is ItemDetailsFieldType.PlainCopyable.Email -> ItemDetailsSnackbarMessage.EmailCopied
            is ItemDetailsFieldType.PlainCopyable.Facebook -> ItemDetailsSnackbarMessage.FacebookCopied
            is ItemDetailsFieldType.PlainCopyable.FirstName -> ItemDetailsSnackbarMessage.FirstNameCopied
            is ItemDetailsFieldType.PlainCopyable.Floor -> ItemDetailsSnackbarMessage.FloorCopied
            is ItemDetailsFieldType.PlainCopyable.FullName -> ItemDetailsSnackbarMessage.FullNameCopied
            is ItemDetailsFieldType.PlainCopyable.Gender -> ItemDetailsSnackbarMessage.GenderCopied
            is ItemDetailsFieldType.PlainCopyable.Instagram -> ItemDetailsSnackbarMessage.InstagramCopied
            is ItemDetailsFieldType.PlainCopyable.LastName -> ItemDetailsSnackbarMessage.LastNameCopied
            is ItemDetailsFieldType.PlainCopyable.LicenseNumber -> ItemDetailsSnackbarMessage.LicenseNumberCopied
            is ItemDetailsFieldType.PlainCopyable.LinkedIn -> ItemDetailsSnackbarMessage.LinkedInCopied
            is ItemDetailsFieldType.PlainCopyable.MiddleName -> ItemDetailsSnackbarMessage.MiddleNameCopied
            is ItemDetailsFieldType.PlainCopyable.Occupation -> ItemDetailsSnackbarMessage.OccupationCopied
            is ItemDetailsFieldType.PlainCopyable.Organization -> ItemDetailsSnackbarMessage.OrganizationCopied
            is ItemDetailsFieldType.PlainCopyable.PassportNumber -> ItemDetailsSnackbarMessage.PassportNumberCopied
            is ItemDetailsFieldType.PlainCopyable.PhoneNumber -> ItemDetailsSnackbarMessage.PhoneNumberCopied
            is ItemDetailsFieldType.PlainCopyable.Reddit -> ItemDetailsSnackbarMessage.RedditCopied
            is ItemDetailsFieldType.PlainCopyable.StateOrProvince -> ItemDetailsSnackbarMessage.StateOrProvinceCopied
            is ItemDetailsFieldType.PlainCopyable.StreetAddress -> ItemDetailsSnackbarMessage.StreetAddressCopied
            is ItemDetailsFieldType.PlainCopyable.TotpCode -> ItemDetailsSnackbarMessage.TotpCodeCopied
            is ItemDetailsFieldType.PlainCopyable.Username -> ItemDetailsSnackbarMessage.UsernameCopied
            is ItemDetailsFieldType.PlainCopyable.Website -> ItemDetailsSnackbarMessage.WebsiteCopied
            is ItemDetailsFieldType.PlainCopyable.XHandle -> ItemDetailsSnackbarMessage.XHandleCopied
            is ItemDetailsFieldType.PlainCopyable.Yahoo -> ItemDetailsSnackbarMessage.YahooCopied
            is ItemDetailsFieldType.PlainCopyable.ZipOrPostalCode -> ItemDetailsSnackbarMessage.ZipOrPostalCodeCopied
            is ItemDetailsFieldType.PlainCopyable.PublicKey -> ItemDetailsSnackbarMessage.PublicKeyCopied
            is ItemDetailsFieldType.PlainCopyable.SSID -> ItemDetailsSnackbarMessage.SSIDCopied
            is ItemDetailsFieldType.AliasItemAction -> null
        }
        snackbarMessage?.let { snackbarDispatcher(it) }
    }

    override fun updateItemDetailsContent(
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>,
        itemCategory: ItemCategory,
        itemContents: ItemContents
    ): ItemContents = getItemDetailsObserver(itemCategory).updateHiddenFieldsContents(
        itemContents = itemContents,
        revealedHiddenCopyableFields = revealedHiddenFields
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

    override fun consumeEvent(event: DetailEvent) {
        detailEventFlow.compareAndSet(event, DetailEvent.Idle)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getItemDetailsObserver(
        itemCategory: ItemCategory
    ): ItemDetailsHandlerObserver<ItemContents, ItemDetailsFieldType> =
        observers[itemCategory] as? ItemDetailsHandlerObserver<ItemContents, ItemDetailsFieldType>
            ?: throw IllegalStateException("Unsupported item category: $itemCategory")

    private companion object {

        private const val TAG = "ItemDetailsHandlerImpl"

    }

}
