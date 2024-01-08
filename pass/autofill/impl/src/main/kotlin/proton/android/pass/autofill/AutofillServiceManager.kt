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
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.extensions.isBrowser
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
import proton.android.pass.data.api.usecases.GetSuggestedCreditCardItems
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.SuggestedCreditCardItemsResult
import proton.android.pass.domain.Item
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject
import kotlin.math.min

class AutofillServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSuggestedLoginItems: GetSuggestedLoginItems,
    private val getSuggestedCreditCardItems: GetSuggestedCreditCardItems,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val ffRepo: FeatureFlagsPreferencesRepository,
    @AppIcon private val appIcon: Int
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
        }
    }

    suspend fun isCreditCardAutofillEnabled(): Boolean = ffRepo
        .get<Boolean>(FeatureFlag.CREDIT_CARD_AUTOFILL)
        .first()

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun handleSuggestions(
        request: InlineSuggestionsRequest,
        autofillData: AutofillData,
        suggestionType: SuggestionType
    ): List<Dataset> {
        val suggestedItemsResult = getSuggestedItems(suggestionType, autofillData)

        return when (suggestedItemsResult) {
            SuggestedItemsResult.Hide -> emptyList()
            SuggestedItemsResult.ShowUpgrade -> upgradeSuggestion(request, autofillData)
            is SuggestedItemsResult.Show -> itemsSuggestions(
                request = request,
                autofillData = autofillData,
                suggestedItems = suggestedItemsResult.items
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun upgradeSuggestion(
        request: InlineSuggestionsRequest,
        autofillData: AutofillData
    ): List<Dataset> = when (request.inlinePresentationSpecs.size) {
        0 -> emptyList()
        else -> listOf(
            createCcUpgradeDataset(
                autofillData = autofillData,
                inlinePresentationSpec = request.inlinePresentationSpecs.first()
            )
        )
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun itemsSuggestions(
        request: InlineSuggestionsRequest,
        autofillData: AutofillData,
        suggestedItems: List<Item>
    ): List<Dataset> {
        val specs = request.inlinePresentationSpecs
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

        return when (specs.size) {
            0 -> emptyList()
            1 -> listOf(pinnedIcon(specs.first()))
            2 -> listOf(openApp(specs.first()), pinnedIcon(specs.last()))
            else -> {
                val openAppDataSet = openApp(specs[specs.size - INLINE_SUGGESTIONS_OFFSET])
                if (suggestedItems.isNotEmpty()) {
                    createItemsDatasetList(
                        suggestedItems = suggestedItems,
                        inlineSuggestionsRequest = request,
                        autofillData = autofillData
                    ).plus(listOf(openAppDataSet, pinnedIcon(specs.last())))
                } else {
                    listOf(openAppDataSet, pinnedIcon(specs.last()))
                }
            }
        }
    }

    private suspend fun getSuggestedItems(
        suggestionType: SuggestionType,
        autofillData: AutofillData
    ): SuggestedItemsResult = when (suggestionType) {
        SuggestionType.CreditCard -> {
            when (val cardsResult = getSuggestedCreditCardItems().firstOrNull()) {
                SuggestedCreditCardItemsResult.Hide -> SuggestedItemsResult.Hide
                is SuggestedCreditCardItemsResult.Items -> SuggestedItemsResult.Show(
                    items = cardsResult.items
                )

                SuggestedCreditCardItemsResult.ShowUpgrade -> SuggestedItemsResult.ShowUpgrade
                null -> SuggestedItemsResult.Hide
            }
        }

        SuggestionType.Login -> {
            val packageName = autofillData.packageInfo.packageName
                .takeIf { !it.isBrowser() }
                .toOption()
                .map { it.value }

            val items = getSuggestedLoginItems(
                packageName = packageName,
                url = autofillData.assistInfo.url
            ).firstOrNull() ?: emptyList()
            SuggestedItemsResult.Show(items)
        }
    }

    suspend fun createMenuPresentationDataset(autofillData: AutofillData): List<Dataset> {
        val suggestedItemsResult = when (autofillData.assistInfo.cluster) {
            NodeCluster.Empty -> SuggestedItemsResult.Show(emptyList())
            is NodeCluster.Login,
            is NodeCluster.SignUp -> getSuggestedItems(SuggestionType.Login, autofillData)

            is NodeCluster.CreditCard -> getSuggestedItems(SuggestionType.CreditCard, autofillData)
        }

        return when (suggestedItemsResult) {
            SuggestedItemsResult.Hide -> emptyList()
            SuggestedItemsResult.ShowUpgrade -> createUpgradePresentationDataset(autofillData)
            is SuggestedItemsResult.Show -> createMenuPresentationDatasetWithItems(
                autofillData = autofillData,
                suggestedItems = suggestedItemsResult.items
            )
        }
    }

    private fun createUpgradePresentationDataset(autofillData: AutofillData): List<Dataset> {
        val upgradePendingIntent = PendingIntentUtils.getUpgradePendingIntent(
            context = context,
            intentRequestCode = OPEN_PASS_UPGRADE_REQUEST_CODE
        )
        val upgradeRemoteView = RemoteViews(context.packageName, R.layout.autofill_item).apply {
            setTextViewText(
                R.id.title,
                context.getText(R.string.suggestions_cc_autofill_paid_plans_title)
            )
            setViewVisibility(R.id.subtitle, View.GONE)
        }
        val upgradeDatasetOptions = DatasetBuilderOptions(
            id = "RemoteView-OpenApp".some(),
            remoteViewPresentation = upgradeRemoteView.some(),
            pendingIntent = upgradePendingIntent.some()
        )
        val upgradeDataset = DatasetUtils.buildDataset(
            options = upgradeDatasetOptions,
            cluster = autofillData.assistInfo.cluster
        )

        return listOf(upgradeDataset)
    }

    private fun createMenuPresentationDatasetWithItems(
        autofillData: AutofillData,
        suggestedItems: List<Item>
    ): List<Dataset> {
        val openAppPendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData,
            intentRequestCode = OPEN_PASS_MENU_REQUEST_CODE
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
            .mapIndexed { index, item ->
                val (view, autofillItem) = encryptionContextProvider.withEncryptionContext {
                    RemoteViews(context.packageName, R.layout.autofill_item)
                        .apply {
                            setTextViewText(
                                R.id.title,
                                ItemDisplayBuilder.createTitle(item, this@withEncryptionContext)
                            )
                            setTextViewText(
                                R.id.subtitle,
                                ItemDisplayBuilder.createSubtitle(item, this@withEncryptionContext)
                            )
                        } to item.toUiModel(this).toAutoFillItem()
                }
                val pendingIntent = PendingIntentUtils.getSuggestionPendingIntent(
                    context = context,
                    autofillData = autofillData,
                    autofillItem = autofillItem,
                    intentRequestCode = index,
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
    private fun createItemsDatasetList(
        suggestedItems: List<Item>,
        inlineSuggestionsRequest: InlineSuggestionsRequest,
        autofillData: AutofillData
    ): List<Dataset> = encryptionContextProvider.withEncryptionContext {
        PassLogger.i(TAG, "Suggested item count: ${suggestedItems.size}")

        val availableInlineSpots: Int = getAvailableSuggestionSpots(
            maxSuggestion = inlineSuggestionsRequest.maxSuggestionCount,
            itemsSize = suggestedItems.size
        )
        if (availableInlineSpots > 0) {
            val shouldAuthenticate = runBlocking {
                needsBiometricAuth().first()
            }
            inlineSuggestionsRequest.inlinePresentationSpecs
                .take(availableInlineSpots - INLINE_SUGGESTIONS_OFFSET)
                .zip(suggestedItems)
                .mapIndexed { index, pair ->
                    createItemDataset(
                        autofillData = autofillData,
                        spec = pair.first,
                        item = pair.second,
                        index = index,
                        shouldAuthenticate = shouldAuthenticate
                    )
                }
                .toList()
        } else {
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun EncryptionContext.createItemDataset(
        autofillData: AutofillData,
        spec: InlinePresentationSpec,
        item: Item,
        index: Int,
        shouldAuthenticate: Boolean
    ): Dataset {
        val pendingIntent = PendingIntentUtils.getSuggestionPendingIntent(
            context = context,
            autofillData = autofillData,
            autofillItem = item.toUiModel(this).toAutoFillItem(),
            intentRequestCode = index,
            shouldAuthenticate = shouldAuthenticate
        )
        val inlinePresentation = InlinePresentationUtils.create(
            title = ItemDisplayBuilder.createTitle(item, this),
            subtitle = ItemDisplayBuilder.createSubtitle(item, this).some(),
            inlinePresentationSpec = spec,
            pendingIntent = PendingIntentUtils
                .getLongPressInlinePendingIntent(context)
        )

        val datasetBuilderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-$index".some(),
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
        val inlinePresentation = InlinePresentationUtils.create(
            title = context.getString(R.string.inline_suggestions_open_app),
            subtitle = None,
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context),
            icon = getIcon().some()
        )
        val pendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData,
            intentRequestCode = OPEN_PASS_SUGGESTION_REQUEST_CODE
        )
        val builderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-OpenApp".some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = builderOptions,
            cluster = autofillData.assistInfo.cluster
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createCcUpgradeDataset(
        autofillData: AutofillData,
        inlinePresentationSpec: InlinePresentationSpec
    ): Dataset {
        val inlinePresentation = InlinePresentationUtils.create(
            title = context.getString(R.string.suggestions_cc_autofill_paid_plans_title),
            subtitle = None,
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context),
            icon = getIcon().some()
        )
        val pendingIntent = PendingIntentUtils.getUpgradePendingIntent(
            context = context,
            intentRequestCode = OPEN_PASS_UPGRADE_REQUEST_CODE
        )
        val builderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-Upgrade".some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = builderOptions,
            cluster = autofillData.assistInfo.cluster
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createPinnedIcon(
        autofillData: AutofillData,
        inlinePresentationSpec: InlinePresentationSpec
    ): Dataset {
        val inlinePresentation = InlinePresentationUtils.createPinned(
            contentDescription = context.getString(R.string.inline_suggestions_open_app),
            icon = getIcon(),
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context)
        )
        val pendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData,
            intentRequestCode = OPEN_PASS_PINNED_REQUEST_CODE
        )
        val builderOptions = DatasetBuilderOptions(
            id = "InlineSuggestion-PinnedIcon".some(),
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            options = builderOptions,
            cluster = autofillData.assistInfo.cluster
        )
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
        private const val OPEN_PASS_SUGGESTION_REQUEST_CODE = 1000
        private const val OPEN_PASS_MENU_REQUEST_CODE = 1001
        private const val OPEN_PASS_PINNED_REQUEST_CODE = 1002
        private const val OPEN_PASS_UPGRADE_REQUEST_CODE = 1003

        private const val TAG = "AutofillServiceManager"
    }
}

sealed interface SuggestionType {
    object Login : SuggestionType
    object CreditCard : SuggestionType
}

sealed interface SuggestedItemsResult {
    object Hide : SuggestedItemsResult
    object ShowUpgrade : SuggestedItemsResult

    @JvmInline
    value class Show(val items: List<Item>) : SuggestedItemsResult
}
