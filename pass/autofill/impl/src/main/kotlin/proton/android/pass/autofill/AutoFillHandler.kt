package proton.android.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import androidx.annotation.ChecksSdkIntAtLeast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.pass.autofill.service.R
import proton.android.pass.autofill.Utils.getWindowNodes
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.extensions.addSaveInfo
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"

    @Suppress("LongParameterList")
    fun handleAutofill(
        context: Context,
        request: FillRequest,
        callback: FillCallback,
        cancellationSignal: CancellationSignal,
        autofillServiceManager: AutofillServiceManager
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
                    autofillServiceManager = autofillServiceManager
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
        autofillServiceManager: AutofillServiceManager
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) return
        val packageName = Utils.getApplicationPackageName(windowNode)
            .takeIf { !BROWSERS.contains(it) }
            .toOption()
        val autofillData = AutofillData(assistInfo, packageName)
        val responseBuilder = FillResponse.Builder()
        val datasetList = if (hasSupportForInlineSuggestions(request)) {
            autofillServiceManager.createSuggestedItemsDatasetList(
                autofillData = autofillData,
                requestOption = request.inlineSuggestionsRequest.toOption()
            )
        } else {
            listOf(autofillServiceManager.createMenuPresentationDataset(autofillData))
        }
        datasetList.forEach {
            responseBuilder.addDataset(it)
        }
        responseBuilder.addSaveInfo(assistInfo)
        return if (!currentCoroutineContext().isActive) {
            callback.onFailure(context.getString(R.string.error_credentials_not_found))
        } else {
            callback.onSuccess(responseBuilder.build())
        }
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
}
