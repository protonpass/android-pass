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
import me.proton.core.util.kotlin.takeIfNotEmpty
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.service.R
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.itemName
import proton.android.pass.commonui.api.loginUsername
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.value
import proton.pass.domain.Item
import proton.pass.domain.ItemType
import javax.inject.Inject
import kotlin.math.min
import proton.android.pass.composecomponents.impl.R as PassR

class AutofillServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSuggestedLoginItems: GetSuggestedLoginItems,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val needsBiometricAuth: NeedsBiometricAuth
) {

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun createSuggestedItemsDatasetList(
        autofillData: AutofillData,
        requestOption: Option<InlineSuggestionsRequest>
    ): List<Dataset> {
        val maxSuggestion = requestOption.value()?.maxSuggestionCount.toOption()
        return if (maxSuggestion is Some && maxSuggestion.value > 0 && requestOption is Some) {
            val suggestedItemsResult = getSuggestedLoginItems(
                packageName = autofillData.packageInfo.map { it.packageName.value },
                url = autofillData.assistInfo.url
            )
                .firstOrNull()
                .toOption()

            val specs = requestOption.value.inlinePresentationSpecs

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

            when (specs.size) {
                0 -> emptyList()
                1 -> listOf(pinnedIcon(specs.first()))
                2 -> listOf(openApp(specs.first()), pinnedIcon(specs.last()))
                else -> {
                    val openAppDataSet = openApp(specs[specs.size - INLINE_SUGGESTIONS_OFFSET])
                    val pinnedOpenApp = pinnedIcon(specs.last())

                    if (suggestedItemsResult is Some && suggestedItemsResult.value.isNotEmpty()) {
                        createSuggestedItemsDatasetList(
                            suggestedItems = suggestedItemsResult.value,
                            maxSuggestion = maxSuggestion.value,
                            requestOption = requestOption.value,
                            autofillData = autofillData,
                            openAppDataSet = openAppDataSet,
                            pinnedOpenApp = pinnedOpenApp
                        )
                    } else {
                        listOf(openAppDataSet, pinnedOpenApp)
                    }
                }
            }
        } else {
            emptyList()
        }
    }

    suspend fun createMenuPresentationDataset(autofillData: AutofillData): List<Dataset> {
        val suggestedItemsResult = getSuggestedLoginItems(
            packageName = autofillData.packageInfo.map { it.packageName.value },
            url = autofillData.assistInfo.url
        ).firstOrNull().toOption()
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
            assistFields = autofillData.assistInfo.fields
        )
        val shouldAuthenticate = runBlocking { needsBiometricAuth().first() }

        return (suggestedItemsResult.value() ?: emptyList())
            .take(2)
            .mapIndexed { index, value ->
                val decryptedTitle = encryptionContextProvider.withEncryptionContext {
                    decrypt(value.title)
                }
                val decryptedUsername = (value.itemType as ItemType.Login).username
                val pendingIntent = PendingIntentUtils.getInlineSuggestionPendingIntent(
                    context = context,
                    autofillData = autofillData,
                    item = value,
                    intentRequestCode = index,
                    shouldAuthenticate = shouldAuthenticate
                )
                val view = RemoteViews(context.packageName, R.layout.autofill_item).apply {
                    setTextViewText(R.id.title, decryptedTitle)
                    setTextViewText(R.id.subtitle, decryptedUsername.takeIfNotEmpty() ?: "---")
                }
                val options = DatasetBuilderOptions(
                    remoteViewPresentation = view.some(),
                    pendingIntent = pendingIntent.some()
                )
                DatasetUtils.buildDataset(
                    options = options,
                    assistFields = autofillData.assistInfo.fields
                )
            }
            .plus(openAppDataSet)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Suppress("LongParameterList")
    private fun createSuggestedItemsDatasetList(
        suggestedItems: List<Item>,
        maxSuggestion: Int,
        requestOption: InlineSuggestionsRequest,
        autofillData: AutofillData,
        openAppDataSet: Dataset,
        pinnedOpenApp: Dataset
    ) = encryptionContextProvider.withEncryptionContext {
        PassLogger.i(TAG, "Suggested item count: ${suggestedItems.size}")

        val availableInlineSpots: Int = getAvailableSuggestionSpots(
            maxSuggestion = maxSuggestion,
            itemsSize = suggestedItems.size
        )
        if (availableInlineSpots > 0) {
            val shouldAuthenticate = runBlocking {
                needsBiometricAuth().first()
            }
            requestOption.inlinePresentationSpecs
                .take(availableInlineSpots - INLINE_SUGGESTIONS_OFFSET)
                .zip(suggestedItems)
                .mapIndexed { index, pair ->
                    createItemDataset(autofillData, pair, index, shouldAuthenticate)
                }
                .toList()
        } else {
            emptyList()
        }
    }
        .plus(listOf(openAppDataSet, pinnedOpenApp))

    @RequiresApi(Build.VERSION_CODES.R)
    private fun EncryptionContext.createItemDataset(
        autofillData: AutofillData,
        pair: Pair<InlinePresentationSpec, Item>,
        index: Int,
        shouldAuthenticate: Boolean
    ): Dataset {
        val pendingIntent = PendingIntentUtils.getInlineSuggestionPendingIntent(
            context = context,
            autofillData = autofillData,
            item = pair.second,
            intentRequestCode = index,
            shouldAuthenticate = shouldAuthenticate
        )
        val inlinePresentation = InlinePresentationUtils.create(
            title = pair.second.itemName(this),
            subtitle = pair.second.loginUsername(),
            inlinePresentationSpec = pair.first,
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
            assistFields = autofillData.assistInfo.fields
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
            assistFields = autofillData.assistInfo.fields
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
            assistFields = autofillData.assistInfo.fields
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
        val icon = Icon.createWithResource(context, PassR.drawable.ic_pass_logo)
        icon.setTintBlendMode(BlendMode.DST)
        return icon
    }

    companion object {
        private const val INLINE_SUGGESTIONS_OFFSET = 2
        private const val OPEN_PASS_SUGGESTION_REQUEST_CODE = 1000
        private const val OPEN_PASS_MENU_REQUEST_CODE = 1001
        private const val OPEN_PASS_PINNED_REQUEST_CODE = 1002

        private const val TAG = "AutofillServiceManager"
    }
}
