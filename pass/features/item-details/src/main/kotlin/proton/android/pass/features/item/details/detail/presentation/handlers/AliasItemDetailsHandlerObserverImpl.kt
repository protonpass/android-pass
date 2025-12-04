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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.AliasDetailEvent.ContactSection
import proton.android.pass.commonuimodels.api.items.AliasDetailEvent.CreateLoginFromAlias
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.ChangeAliasStatus
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContacts
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementContacts
import proton.android.pass.totp.api.ObserveTotpFromUri
import javax.inject.Inject

class AliasItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val observeTotpFromUri: ObserveTotpFromUri,
    override val canDisplayTotp: CanDisplayTotp,
    private val observeAliasDetails: ObserveAliasDetails,
    private val observeAliasContacts: ObserveAliasContacts,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val changeAliasStatus: ChangeAliasStatus
) : ItemDetailsHandlerObserver<ItemContents.Alias, ItemDetailsFieldType.AliasItemAction>(
    encryptionContextProvider = encryptionContextProvider,
    observeTotpFromUri = observeTotpFromUri,
    canDisplayTotp = canDisplayTotp
) {

    private val isAliasStateTogglingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState,
        savedStateEntries: Map<String, Any?>,
        detailEvent: DetailEvent
    ): Flow<ItemDetailState> = combineN(
        observeItemContents(item),
        observeAliasDetails(item.shareId, item.id).onStart { emit(AliasDetails.EMPTY) },
        observeAliasContacts(item.shareId, item.id),
        observeCustomFieldTotps(item),
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementContacts),
        isAliasStateTogglingState
    ) { aliasItemContents, aliasDetails, aliasContacts, customFieldTotps, displayContactsBanner,
        isAliasStateToggling ->
        ItemDetailState.Alias(
            itemContents = aliasItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemShare = share,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            isAliasStateToggling = isAliasStateToggling.value(),
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Alias(),
            itemShareCount = item.shareCount,
            aliasDetails = aliasDetails,
            aliasContacts = aliasContacts,
            attachmentsState = attachmentsState,
            customFieldTotps = customFieldTotps,
            displayContactsBanner = displayContactsBanner.value,
            detailEvent = detailEvent
        )
    }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Alias,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents = itemContents.copy(
        customFields = updateHiddenCustomFieldContents(
            customFields = itemContents.customFields,
            revealedHiddenFields = revealedHiddenCopyableFields[ItemSection.CustomField].orEmpty()
        )
    )

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Alias,
        otherItemContents: ItemContents.Alias,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Alias(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            aliasEmail = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.aliasEmail,
                otherItemFieldValue = otherItemContents.aliasEmail
            ),
            attachments = calculateItemDiffType(
                baseItemAttachments = baseAttachments,
                otherItemAttachments = otherAttachments
            ),
            customFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.customFields,
                otherItemCustomFieldsContent = otherItemContents.customFields
            )
        )
    }

    override suspend fun performAction(
        fieldType: ItemDetailsFieldType.AliasItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) {
        when (fieldType) {
            ItemDetailsFieldType.AliasItemAction.ContactBanner -> dismissContactsBanner()
            is ItemDetailsFieldType.AliasItemAction.ContactSection -> {
                dismissContactsBanner()
                callback(ContactSection(fieldType.shareId, fieldType.itemId))
            }

            is ItemDetailsFieldType.AliasItemAction.CreateLoginFromAlias ->
                callback(CreateLoginFromAlias(fieldType.alias, fieldType.shareId))
            is ItemDetailsFieldType.AliasItemAction.ToggleAlias -> {
                isAliasStateTogglingState.emit(IsLoadingState.Loading)
                safeRunCatching { changeAliasStatus(fieldType.shareId, fieldType.itemId, fieldType.value) }
                    .onSuccess {
                        PassLogger.i(TAG, "Alias status changed successfully")
                    }
                    .onFailure {
                        PassLogger.w(TAG, "Error changing alias status")
                        PassLogger.w(TAG, it)
                    }
                isAliasStateTogglingState.emit(IsLoadingState.NotLoading)
            }
        }
    }

    fun dismissContactsBanner() {
        userPreferencesRepository.setDisplayFeatureDiscoverBanner(
            AliasManagementContacts,
            FeatureDiscoveryBannerPreference.NotDisplay
        )
    }

    companion object {
        private const val TAG = "AliasItemDetailsHandlerObserverImpl"
    }
}
