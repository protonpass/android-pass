/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.CustomItemFormProcessor
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.TemplateTypeNavArgId
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.OnVaultSelected
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.PrefillTemplate
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.SubmitCreate
import proton.android.pass.features.itemcreate.custom.shared.TemplateType
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateCustomItemViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val createItem: CreateItem,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getItemById: GetItemById,
    canPerformPaidAction: CanPerformPaidAction,
    linkAttachmentsToItem: LinkAttachmentsToItem,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    userPreferencesRepository: UserPreferencesRepository,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    customFieldDraftRepository: CustomFieldDraftRepository,
    clipboardManager: ClipboardManager,
    customItemFormProcessor: CustomItemFormProcessor,
    appDispatchers: AppDispatchers,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCustomItemViewModel(
    canPerformPaidAction = canPerformPaidAction,
    linkAttachmentsToItem = linkAttachmentsToItem,
    snackbarDispatcher = snackbarDispatcher,
    attachmentsHandler = attachmentsHandler,
    customFieldHandler = customFieldHandler,
    userPreferencesRepository = userPreferencesRepository,
    featureFlagsRepository = featureFlagsRepository,
    encryptionContextProvider = encryptionContextProvider,
    customFieldDraftRepository = customFieldDraftRepository,
    clipboardManager = clipboardManager,
    customItemFormProcessor = customItemFormProcessor,
    appDispatchers = appDispatchers,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map(::ShareId)

    private val navItemId: Option<ItemId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ItemId.key)
            .toOption()
            .map(::ItemId)

    private val navTemplateType: Option<TemplateType> =
        savedStateHandleProvider.get().require<Int>(TemplateTypeNavArgId.key)
            .let { if (it > 0) TemplateType.fromId(it).some() else None }

    init { processIntent(PrefillTemplate) }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: Flow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeVaults().asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    val state = combine(
        shareUiState,
        observeSharedState()
    ) { shareUiState, sharedState ->
        when (shareUiState) {
            is ShareUiState.Error -> CustomItemState.Error
            is ShareUiState.Loading -> CustomItemState.Loading
            is ShareUiState.Success ->
                CustomItemState.CreateCustomItemState(shareUiState, sharedState)

            ShareUiState.NotInitialised -> CustomItemState.NotInitialised
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomItemState.NotInitialised
        )

    fun processIntent(intent: BaseItemFormIntent) {
        when (intent) {
            is BaseCustomItemCommonIntent -> processCommonIntent(intent)
            is CreateSpecificIntent -> processSpecificIntent(intent)
            else -> throw IllegalArgumentException("Unknown intent: $intent")
        }
    }

    private fun processSpecificIntent(intent: CreateSpecificIntent) {
        when (intent) {
            is SubmitCreate -> onSubmitCreate(intent.shareId)
            is OnVaultSelected -> onVaultSelected(intent.shareId)
            PrefillTemplate -> onPrefillTemplate()
        }
    }

    private fun onPrefillTemplate() {
        val templateType = navTemplateType.value() ?: return
        val (fields, staticFields) = encryptionContextProvider.withEncryptionContext {
            val fields = templateType.fields.map {
                UICustomFieldContent.createCustomField(
                    type = it.type,
                    label = context.getString(it.nameResId),
                    encryptionContext = this
                )
            }
            val staticFields = when (templateType) {
                TemplateType.SSH_KEY -> ItemStaticFields.SSHKey(
                    publicKey = "",
                    privateKey = UIHiddenState.Empty(encrypt(""))
                )
                TemplateType.WIFI_NETWORK -> ItemStaticFields.WifiNetwork(
                    ssid = "",
                    password = UIHiddenState.Empty(encrypt("")),
                    wifiSecurityType = WifiSecurityType.Unknown
                )
                else -> ItemStaticFields.Custom
            }
            fields to staticFields
        }
        itemFormState = itemFormState.copy(
            itemStaticFields = staticFields,
            customFieldList = itemFormState.customFieldList.toMutableList().apply {
                addAll(fields)
            }
        )
    }

    private fun onSubmitCreate(shareId: ShareId) {
        viewModelScope.launch {
            if (!isFormStateValid()) return@launch
            updateLoadingState(IsLoadingState.Loading)
            runCatching {
                createItem(
                    shareId = shareId,
                    itemContents = itemFormState.toItemContents()
                )
            }
                .onFailure {
                    PassLogger.w(TAG, "Could not create item")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(CustomItemSnackbarMessage.ItemCreationError)
                }
                .onSuccess { item ->
                    snackbarDispatcher(CustomItemSnackbarMessage.ItemCreated)
                    linkAttachments(item.shareId, item.id, item.revision)
                    inAppReviewTriggerMetrics.incrementItemCreatedCount()
                    onItemSavedState(item)
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Custom))
                }
            updateLoadingState(IsLoadingState.NotLoading)
        }
    }

    private fun onVaultSelected(shareId: ShareId) {
        selectedShareIdMutableState = Some(shareId)
    }

    suspend fun cloneContents() {
        val shareId = navShareId.value() ?: return
        val itemId = navItemId.value() ?: return
        val item = getItemById(shareId = shareId, itemId = itemId)
        encryptionContextProvider.withEncryptionContext {
            val staticFields: ItemStaticFields
            val customFields: List<UICustomFieldContent>
            val extraSections: List<UIExtraSection>

            val type = item.itemType
            when (type) {
                is ItemType.WifiNetwork -> {
                    staticFields = ItemStaticFields.WifiNetwork(
                        ssid = type.ssid,
                        password = UIHiddenState.Concealed(type.password),
                        wifiSecurityType = type.wifiSecurityType
                    )
                    val itemContents = item.toItemContents<ItemContents.WifiNetwork> { decrypt(it) }
                    customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
                    extraSections = itemContents.sectionContentList.map { UIExtraSection(it) }
                }

                is ItemType.SSHKey -> {
                    staticFields = ItemStaticFields.SSHKey(
                        publicKey = type.publicKey,
                        privateKey = UIHiddenState.Concealed(type.privateKey)
                    )
                    val itemContents = item.toItemContents<ItemContents.SSHKey> { decrypt(it) }
                    customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
                    extraSections = itemContents.sectionContentList.map { UIExtraSection(it) }
                }

                is ItemType.Custom -> {
                    staticFields = ItemStaticFields.Custom
                    val itemContents = item.toItemContents<ItemContents.Custom> { decrypt(it) }
                    customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
                    extraSections = itemContents.sectionContentList.map { UIExtraSection(it) }
                }
                else -> throw IllegalStateException("Not a custom item type")
            }
            itemFormState = itemFormState.copy(
                title = context.getString(R.string.title_clone, decrypt(item.title)),
                itemStaticFields = staticFields,
                customFieldList = customFields,
                sectionList = extraSections
            )
        }
    }

    companion object {
        private const val TAG = "CreateCustomItemViewModel"
    }
}

sealed interface CreateSpecificIntent : BaseItemFormIntent {
    data object PrefillTemplate : CreateSpecificIntent

    @JvmInline
    value class SubmitCreate(val shareId: ShareId) : CreateSpecificIntent

    @JvmInline
    value class OnVaultSelected(val shareId: ShareId) : CreateSpecificIntent
}

