package me.proton.pass.autofill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
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
import me.proton.pass.autofill.Utils.getWindowNodes
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.extensions.addInlineSuggestion
import me.proton.pass.autofill.extensions.addOpenAppInlineSuggestion
import me.proton.pass.autofill.extensions.addSaveInfo
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.autofill.AutofillActivity
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.some
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.Item
import me.proton.pass.domain.usecases.GetSuggestedLoginItems
import me.proton.pass.domain.usecases.UrlOrPackage
import kotlin.coroutines.coroutineContext
import kotlin.math.min

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"

    fun handleAutofill(
        context: Context,
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
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

    private suspend fun searchAndFill(
        context: Context,
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
                min + 1
            } else {
                min
            }
            if (size > 0) {
                for (i in 0 until size - 1) {
                    val pendingIntent = PendingIntent.getService(
                        context,
                        0,
                        Intent(),
                        getAutofillPendingIntentFlag()
                    )
                    val inlinePresentation: InlinePresentation =
                        InlinePresentationUtils.create(
                            title = "github.com",
                            subtitle = "vic@test.com".some(),
                            inlinePresentationSpec = inlineSuggestionRequest.inlinePresentationSpecs[i],
                            pendingIntent = pendingIntent
                        )
                    responseBuilder.addInlineSuggestion(
                        inlinePresentation = inlinePresentation,
                        assistInfo = assistInfo,
                        pendingIntent = pendingIntent.toOption()
                    )
                }
                responseBuilder.addOpenAppInlineSuggestion(
                    context = context,
                    autofillData = autofillData,
                    inlinePresentationSpec = inlineSuggestionRequest.inlinePresentationSpecs[size],
                    pendingIntent = getOpenAppPendingIntent(context, autofillData)
                )
            }

        } else {
            val defaultDataset = DatasetUtils.buildDataset(
                authenticateView = getDialogView(context).toOption(),
                inlinePresentation = None,
                pendingIntent = getOpenAppPendingIntent(context, autofillData).toOption(),
                assistInfo = autofillData.assistInfo
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

    private fun getOpenAppPendingIntent(context: Context, autofillData: AutofillData) =
        PendingIntent.getActivity(
            context,
            AutofillActivity.REQUEST_CODE,
            AutofillActivity.newIntent(context, autofillData),
            getAutofillPendingIntentFlag()
        )

    private fun getAutofillPendingIntentFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
}
