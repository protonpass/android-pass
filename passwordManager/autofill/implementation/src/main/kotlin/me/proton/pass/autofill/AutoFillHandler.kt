package me.proton.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.widget.RemoteViews
import androidx.annotation.ChecksSdkIntAtLeast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.pass.autofill.PendingIntentUtils.getOpenAppPendingIntent
import me.proton.pass.autofill.Utils.getWindowNodes
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.extensions.addItemInlineSuggestion
import me.proton.pass.autofill.extensions.addOpenAppInlineSuggestion
import me.proton.pass.autofill.extensions.addSaveInfo
import me.proton.pass.autofill.service.R
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.Item
import me.proton.pass.domain.usecases.GetSuggestedLoginItems
import me.proton.pass.domain.usecases.UrlOrPackage
import kotlin.coroutines.coroutineContext
import kotlin.math.min

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"
    private const val INLINE_SUGGESTIONS_OFFSET = 1

    @Suppress("LongParameterList")
    fun handleAutofill(
        context: Context,
        cryptoContext: CryptoContext,
        request: FillRequest,
        callback: FillCallback,
        cancellationSignal: CancellationSignal,
        getSuggestedLoginItems: GetSuggestedLoginItems
    ) {
        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            callback.onFailure(context.getString(R.string.error_cant_find_matching_fields))
            return
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.e(TAG, exception)
        }
        val job = CoroutineScope(Dispatchers.IO)
            .launch(handler) {
                searchAndFill(
                    context = context,
                    cryptoContext = cryptoContext,
                    windowNode = windowNode,
                    callback = callback,
                    request = request,
                    getSuggestedLoginItems = getSuggestedLoginItems
                )
            }

        cancellationSignal.setOnCancelListener {
            job.cancel()
        }
    }

    @Suppress("ReturnCount", "LongParameterList")
    private suspend fun searchAndFill(
        context: Context,
        cryptoContext: CryptoContext,
        windowNode: AssistStructure.WindowNode,
        callback: FillCallback,
        request: FillRequest,
        getSuggestedLoginItems: GetSuggestedLoginItems
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) return
        val autofillData = AutofillData(assistInfo, Utils.getApplicationPackageName(windowNode))
        val responseBuilder = FillResponse.Builder()
        if (hasSupportForInlineSuggestions(request)) {
            val inlineSuggestionRequest = request.inlineSuggestionsRequest ?: return
            val maxSuggestion = inlineSuggestionRequest.maxSuggestionCount
            val suggestedItemsResult: Result.Success<List<Item>> =
                getSuggestedLoginItems(UrlOrPackage(autofillData.packageName))
                    .filterIsInstance<Result.Success<List<Item>>>()
                    .firstOrNull()
                    ?: return
            val min = min(maxSuggestion, suggestedItemsResult.data.size)
            val size = if (maxSuggestion > suggestedItemsResult.data.size) {
                min + INLINE_SUGGESTIONS_OFFSET
            } else {
                min
            }
            if (size > 0) {
                val specs =
                    inlineSuggestionRequest.inlinePresentationSpecs.take(size - INLINE_SUGGESTIONS_OFFSET)
                for ((index, spec) in specs.withIndex()) {

                    val item: Option<Item> = suggestedItemsResult.data.getOrNull(index).toOption()
                    responseBuilder.addItemInlineSuggestion(
                        context = context,
                        cryptoContext = cryptoContext,
                        itemOption = item,
                        inlinePresentationSpec = spec,
                        assistFields = assistInfo.fields
                    )
                }
                responseBuilder.addOpenAppInlineSuggestion(
                    context = context,
                    cryptoContext = cryptoContext,
                    inlinePresentationSpec = inlineSuggestionRequest.inlinePresentationSpecs[size],
                    pendingIntent = getOpenAppPendingIntent(context, autofillData),
                    assistFields = assistInfo.fields
                )
            }
        } else {
            val defaultDataset = DatasetUtils.buildDataset(
                context = context,
                autofillMappings = None,
                dsbOptions = DatasetBuilderOptions(
                    authenticateView = getDialogView(context).toOption(),
                    pendingIntent = getOpenAppPendingIntent(context, autofillData).toOption()
                ),
                assistFields = autofillData.assistInfo.fields
            )
            responseBuilder.addDataset(defaultDataset)
        }
        if (!coroutineContext.isActive) {
            callback.onFailure(context.getString(R.string.error_credentials_not_found))
            return
        }
        responseBuilder.addSaveInfo(assistInfo)
        callback.onSuccess(responseBuilder.build())
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    private fun hasSupportForInlineSuggestions(request: FillRequest): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            request.inlineSuggestionsRequest?.let {
                val maxSuggestion = it.maxSuggestionCount
                val specCount = it.inlinePresentationSpecs.count()
                maxSuggestion > 0 && specCount > 0
            } ?: false
        } else {
            false
        }

    private fun getDialogView(context: Context): RemoteViews {
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
}
