package proton.android.pass.autofill

import android.content.Context
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.Dataset
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.service.R
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
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.pass.domain.Item
import javax.inject.Inject
import kotlin.math.min
import proton.android.pass.composecomponents.impl.R as PassR

class AutofillServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSuggestedLoginItems: GetSuggestedLoginItems,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val preferencesRepository: UserPreferencesRepository
) {

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun createSuggestedItemsDatasetList(
        autofillData: AutofillData,
        requestOption: Option<InlineSuggestionsRequest>
    ): List<Dataset> {
        val maxSuggestion = requestOption.value()?.maxSuggestionCount.toOption()
        return if (maxSuggestion is Some && maxSuggestion.value > 0 && requestOption is Some) {
            val suggestedItemsResult =
                getSuggestedLoginItems(
                    packageName = autofillData.packageInfo.map { it.packageName.value },
                    url = autofillData.assistInfo.url
                )
                    .firstOrNull()
                    .toOption()

            val specs = requestOption.value.inlinePresentationSpecs
            val openAppDataSet = createOpenAppDataset(
                autofillData = autofillData,
                inlinePresentationSpec = specs[specs.size - INLINE_SUGGESTIONS_OFFSET]
            )
            val pinnedOpenApp = createPinnedIcon(
                autofillData = autofillData,
                inlinePresentationSpec = specs.last()
            )
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
        } else {
            emptyList()
        }
    }

    fun createMenuPresentationDataset(autofillData: AutofillData): Dataset {
        val pendingIntent = PendingIntentUtils.getOpenAppPendingIntent(
            context = context,
            autofillData = autofillData,
            intentRequestCode = OPEN_PASS_MENU_REQUEST_CODE
        )
        val datasetBuilderOptions = DatasetBuilderOptions(
            authenticateView = getMenuPresentationView(context).toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            context = context,
            dsbOptions = datasetBuilderOptions,
            assistFields = autofillData.assistInfo.fields
        )
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
                preferencesRepository.getBiometricLockState().first().value()
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
    }.plus(listOf(openAppDataSet, pinnedOpenApp))

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
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            context = context,
            dsbOptions = datasetBuilderOptions,
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
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            context = context,
            dsbOptions = builderOptions,
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
            inlinePresentation = inlinePresentation.toOption(),
            pendingIntent = pendingIntent.toOption()
        )
        return DatasetUtils.buildDataset(
            context = context,
            dsbOptions = builderOptions,
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

    private fun getMenuPresentationView(context: Context): RemoteViews {
        val view = RemoteViews(
            context.packageName,
            android.R.layout.simple_list_item_1
        )
        view.setTextViewText(
            android.R.id.text1,
            context.getString(R.string.autofill_authenticate_prompt)
        )
        return view
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
