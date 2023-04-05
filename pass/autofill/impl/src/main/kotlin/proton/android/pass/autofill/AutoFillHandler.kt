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
import proton.android.pass.autofill.Utils.getWindowNodes
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.extensions.addSaveInfo
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"

    @Suppress("LongParameterList")
    fun handleAutofill(
        context: Context,
        request: FillRequest,
        callback: FillCallback,
        cancellationSignal: CancellationSignal,
        autofillServiceManager: AutofillServiceManager,
        telemetryManager: TelemetryManager
    ) {
        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            PassLogger.i(TAG, "No window node found")
            callback.onSuccess(null)
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
                    autofillServiceManager = autofillServiceManager,
                    telemetryManager = telemetryManager
                )
            }

        cancellationSignal.setOnCancelListener {
            job.cancel()
        }
    }

    @Suppress("LongParameterList")
    private suspend fun searchAndFill(
        context: Context,
        windowNode: AssistStructure.WindowNode,
        callback: FillCallback,
        request: FillRequest,
        autofillServiceManager: AutofillServiceManager,
        telemetryManager: TelemetryManager
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) return
        val packageNameOption = Utils.getApplicationPackageName(windowNode)
            .takeIf { !BROWSERS.contains(it) }
            .toOption()
        val packageInfoOption = packageNameOption.map {
            PackageInfo(
                packageName = PackageName(it),
                appName = AndroidUtils.getApplicationName(context, it).value()
                    ?.let { appName -> AppName(appName) }
                    ?: AppName(it)
            )
        }
        val autofillData = AutofillData(assistInfo, packageInfoOption)
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
            PassLogger.i(TAG, "Job was cancelled")
            callback.onSuccess(null)
        } else {
            telemetryManager.sendEvent(AutofillDisplayed(AutofillTriggerSource.Source))
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
