/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill

import android.content.Context
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.Dataset
import android.view.View
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.autofill.service.R
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import javax.inject.Inject
import kotlin.math.min

class AutofillServiceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getSuggestedAutofillItems: GetSuggestedAutofillItems,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val needsBiometricAuth: NeedsBiometricAuth,
    @AppIcon private val appIcon: Int,
    private val packageNameUrlSuggestionAdapter: PackageNameUrlSuggestionAdapter
) {

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun createSuggestedItemsDatasetList(
        autofillData: AutofillData,
        inlineSuggestionsRequest: InlineSuggestionsRequest
    ): List<Dataset> {
        if (inlineSuggestionsRequest.maxSuggestionCount == 0) return emptyList()
        return when (autofillData.assistInfo.cluster) {
            NodeCluster.Empty -> emptyList()
            is NodeCluster.Login,
            is NodeCluster.SignUp -> handleSuggestions(
                request = inlineSuggestionsRequest,
                autofillData = autofillData,
                suggestionType = SuggestionType.Login
            )

            is NodeCluster.CreditCard -> handleSuggestions(
                request = inlineSuggestionsRequest,
                autofillData = autofillData,
                suggestionType = SuggestionType.CreditCard
            )

            is NodeCluster.Identity -> handleSuggestions(
                request = inlineSuggestionsRequest,
                autofillData = autofillData,
                suggestionType = SuggestionType.Identity
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun handleSuggestions(
        request: InlineSuggestionsRequest,
        autofillData: AutofillData,
        suggestionType: SuggestionType
    ): List<Dataset> {
        val suggestedItems = getSuggestedItems(suggestionType, autofillData)
        return itemsSuggestions(
            request = request,
            autofillData = autofillData,
            suggestedItems = suggestedItems
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun itemsSuggestions(
        request: InlineSuggestionsRequest,
        autofillData: AutofillData,
        suggestedItems: List<ItemData.SuggestedItem>
    ): List<Dataset> {
        val specs = request.inlinePresentationSpecs
        if (specs.isEmpty()) return emptyList()

        val pinnedIcon = { spec: InlinePresentationSpec ->
            createPinnedIcon(
                autofillData = autofillData,
                inlinePresentationSpec = spec
            )
        }
        val openApp = { spec: InlinePresentationSpec ->
            createOpenAppDataset(
                autofillData = autofillData,
                inlinePresentationSpec = spec
            )
        }

        val getPresentationSpec: (Int) -> InlinePresentationSpec = { idx: Int ->
            specs.getOrElse(idx) { specs.last() }
        }

        return when (request.maxSuggestionCount) {
            0 -> emptyList()
            1 -> listOf(pinnedIcon(getPresentationSpec(0)))
            2 -> listOf(openApp(getPresentationSpec(0)), pinnedIcon(getPresentationSpec(1)))
            else -> buildList {
                val itemSuggestions = createItemSuggestions(
                    autofillData = autofillData,
                    maxSuggestionCount = request.maxSuggestionCount,
                    suggestedItems = suggestedItems,
                    getPresentationSpec = getPresentationSpec
                )
                addAll(itemSuggestions)

                add(openApp(getPresentationSpec(itemSuggestions.lastIndex + 1)))
                add(pinnedIcon(getPresentationSpec(itemSuggestions.lastIndex + 2)))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createItemSuggestions(
        autofillData: AutofillData,
        maxSuggestionCount: Int,
        suggestedItems: List<ItemData.SuggestedItem>,
        getPresentationSpec: (Int) -> InlinePresentationSpec
    ): List<Dataset> {
        val availableInlineSpots: Int = getAvailableSuggestionSpots(
            maxSuggestion = maxSuggestionCount,
            itemsSize = suggestedItems.size
        )
        if (availableInlineSpots == 0) return emptyList()
        val itemsToSuggest = suggestedItems
            .take(availableInlineSpots - INLINE_SUGGESTIONS_OFFSET)

        val shouldAuthenticate = runBlocking { needsBiometricAuth().first() }
        return encryptionContextProvider.withEncryptionContext {
            itemsToSuggest.mapIndexed { idx, item ->
                createItemDataset(
                    autofillData = autofillData,
                    spec = getPresentationSpec(idx),
                    suggestedItem = item,
                    index = idx,
                    shouldAuthenticate = shouldAuthenticate
                )
            }
        }
    }

    private suspend fun getSuggestedItems(
        suggestionType: SuggestionType,
        autofillData: AutofillData
    ): List<ItemData.SuggestedItem> {
        val suggestionSource = packageNameUrlSuggestionAdapter.adapt(
            packageName = autofillData.packageInfo.packageName,
            url = autofillData.assistInfo.url.value().orEmpty()
        )
        val itemTypeFilter = when (suggestionType) {
            SuggestionType.CreditCard -> ItemTypeFilter.CreditCards
            SuggestionType.Identity -> ItemTypeFilter.Identity
            SuggestionType.Login -> ItemTypeFilter.Logins
        }
        val result = getSuggestedAutofillItems(
            itemTypeFilter = itemTypeFilter,
            suggestion = suggestionSource.toSuggestion()
        ).firstOrNull()
        return when (result) {
            is SuggestedAutofillItemsResult.Items -> result.suggestedItems
            null -> emptyList()
        }
    }

    suspend fun createMenuPresentationDataset(autofillData: AutofillData): List<Dataset> {
        val suggestedItems = when (autofillData.assistInfo.cluster) {
            NodeCluster.Empty -> emptyList()
            is NodeCluster.Login,
            is NodeCluster.SignUp -> getSuggestedItems(SuggestionType.Login, autofillData)

            is NodeCluster.CreditCard -> getSuggestedItems(SuggestionType.CreditCard, autofillData)
            is NodeCluster.Identity -> getSuggestedItems(SuggestionType.Identity, autofillData)
        }

        return createMenuPresentationDatasetWithItems(
            autofillData = autofillData,
            suggestedItems = suggestedItems
        )
    }

    @Suppress("LongMethod")
    private fun createMenuPresentationDatasetWithItems(
        autofillData: AutofillData,
        suggestedItems: List<ItemData.SuggestedItem>
    ): List<Dataset> {
        val openAppPendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData
        )
        val openAppRemoteView = RemoteViews(context.packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.title, context.getText(R.string.autofill_authenticate_prompt))
            setViewVisibility(R.id.subtitle, View.GONE)
        }
        val openAppDatasetOptions = DatasetBuilderOptions(
            id = "RemoteView-OpenApp".some(),
            remoteViewPresentation = openAppRemoteView.some(),
            pendingIntent = openAppPendingIntent.some()
        )
        val openAppDataSet = DatasetUtils.buildDataset(
            options = openAppDatasetOptions,
            cluster = autofillData.assistInfo.cluster
        )
        val shouldAuthenticate = runBlocking { needsBiometricAuth().first() }

        return suggestedItems.take(2)
            .mapIndexed { index, suggestedItem ->
                val (view, autofillItem) = encryptionContextProvider.withEncryptionContext {
                    val view = RemoteViews(context.packageName, R.layout.autofill_item)
                        .apply {
                            setTextViewText(
                                R.id.title,
                                ItemDisplayBuilder.createTitle(
                                    suggestedItem.item,
                                    this@withEncryptionContext
                                )
                            )
                            setTextViewText(
                                R.id.subtitle,
                                ItemDisplayBuilder.createSubtitle(
                                    suggestedItem.item,
                                    this@withEncryptionContext
                                )
                            )
                        }
                    val autofillItem = suggestedItem.item.toUiModel(this)
                        .toAutoFillItem(suggestedItem.isDALSuggestion)
                    view to autofillItem
                }
                val pendingIntent = PendingIntentUtils.getSuggestionPendingIntent(
                    context = context,
                    autofillData = autofillData,
                    autofillItem = autofillItem,
                    shouldAuthenticate = shouldAuthenticate
                )
                val options = DatasetBuilderOptions(
                    remoteViewPresentation = view.some(),
                    pendingIntent = pendingIntent.some()
                )
                DatasetUtils.buildDataset(
                    options = options,
                    cluster = autofillData.assistInfo.cluster
                )
            }
            .plus(openAppDataSet)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun EncryptionContext.createItemDataset(
        autofillData: AutofillData,
        spec: InlinePresentationSpec,
        suggestedItem: ItemData.SuggestedItem,
        index: Int,
        shouldAuthenticate: Boolean
    ): Dataset {
        val title = ItemDisplayBuilder.createTitle(suggestedItem.item, this)
        val subtitle = ItemDisplayBuilder.createSubtitle(suggestedItem.item, this)
        val autofillItem = suggestedItem.item.toUiModel(this)
            .toAutoFillItem(suggestedItem.isDALSuggestion)
        val pendingIntent = PendingIntentUtils.getSuggestionPendingIntent(
            context = context,
            autofillData = autofillData,
            autofillItem = autofillItem,
            shouldAuthenticate = shouldAuthenticate
        )
        val inlinePresentation = InlinePresentationUtils.create(
            title = title,
            subtitle = subtitle.some(),
            inlinePresentationSpec = spec,
            pendingIntent = PendingIntentUtils
                .getLongPressInlinePendingIntent(context)
        )
        val remoteViewPresentation = createRemoteViewPresentation(
            title = title,
            subtitle = subtitle
        )

        val datasetBuilderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-$index".some(),
            remoteViewPresentation = remoteViewPresentation.some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = datasetBuilderOptions,
            cluster = autofillData.assistInfo.cluster
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createOpenAppDataset(
        autofillData: AutofillData,
        inlinePresentationSpec: InlinePresentationSpec
    ): Dataset {
        val title = context.getString(R.string.inline_suggestions_open_app)
        val inlinePresentation = InlinePresentationUtils.create(
            title = title,
            subtitle = None,
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context),
            icon = getIcon().some()
        )
        val pendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData
        )
        val builderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-OpenApp".some(),
            remoteViewPresentation = createRemoteViewPresentation(
                title = title,
                subtitle = null
            ).some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = builderOptions,
            cluster = autofillData.assistInfo.cluster
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createPinnedIcon(autofillData: AutofillData, inlinePresentationSpec: InlinePresentationSpec): Dataset {
        val title = context.getString(R.string.inline_suggestions_open_app)
        val inlinePresentation = InlinePresentationUtils.createPinned(
            contentDescription = title,
            icon = getIcon(),
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context)
        )
        val pendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData
        )
        val builderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-PinnedIcon".some(),
            remoteViewPresentation = createRemoteViewPresentation(
                title = title,
                subtitle = null
            ).some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = builderOptions,
            cluster = autofillData.assistInfo.cluster
        )
    }

    private fun createRemoteViewPresentation(title: String, subtitle: String?): RemoteViews =
        RemoteViews(context.packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.title, title)
            if (subtitle == null) {
                setViewVisibility(R.id.subtitle, View.GONE)
            } else {
                setViewVisibility(R.id.subtitle, View.VISIBLE)
                setTextViewText(R.id.subtitle, subtitle)
            }
        }

    private fun getAvailableSuggestionSpots(maxSuggestion: Int, itemsSize: Int): Int {
        val min = min(maxSuggestion, itemsSize)
        return if (maxSuggestion > itemsSize) {
            min + INLINE_SUGGESTIONS_OFFSET
        } else {
            min
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getIcon(): Icon {
        val icon = Icon.createWithResource(context, appIcon)
        icon.setTintBlendMode(BlendMode.DST)
        return icon
    }

    companion object {
        private const val INLINE_SUGGESTIONS_OFFSET = 2
    }
}

sealed interface SuggestionType {
    data object Login : SuggestionType
    data object CreditCard : SuggestionType
    data object Identity : SuggestionType
}
