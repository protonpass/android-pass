package proton.android.pass.autofill

import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.proton.pass.autofill.service.R
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
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
                getSuggestedLoginItems(autofillData.packageName, autofillData.assistInfo.url)
                    .filterIsInstance<LoadingResult.Success<List<Item>>>()
                    .map { it.data }
                    .firstOrNull()
                    .toOption()

            val openAppDataSet = createOpenAppDataset(
                autofillData = autofillData,
                inlinePresentationSpec = requestOption.value.inlinePresentationSpecs.last()
            )
            if (suggestedItemsResult is Some && suggestedItemsResult.value.isNotEmpty()) {
                createSuggestedItemsDatasetList(
                    suggestedItems = suggestedItemsResult.value,
                    maxSuggestion = maxSuggestion.value,
                    requestOption = requestOption.value,
                    autofillData = autofillData,
                    openAppDataSet = openAppDataSet
                )
            } else {
                listOf(openAppDataSet)
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
    private fun createSuggestedItemsDatasetList(
        suggestedItems: List<Item>,
        maxSuggestion: Int,
        requestOption: InlineSuggestionsRequest,
        autofillData: AutofillData,
        openAppDataSet: Dataset
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
    }.plus(openAppDataSet)

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
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context)
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

    companion object {
        private const val INLINE_SUGGESTIONS_OFFSET = 1
        private const val OPEN_PASS_SUGGESTION_REQUEST_CODE = 1000
        private const val OPEN_PASS_MENU_REQUEST_CODE = 1001

        private const val TAG = "AutofillServiceManager"
    }
}
