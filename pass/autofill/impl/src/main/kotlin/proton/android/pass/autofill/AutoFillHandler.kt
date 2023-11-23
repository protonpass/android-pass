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

import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import androidx.annotation.ChecksSdkIntAtLeast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.autofill.Utils.getWindowNodes
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.extensions.addSaveInfo
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.autofill.heuristics.NodeClusterer
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.autofill.heuristics.focused
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"

    @Suppress("LongParameterList")
    fun handleAutofill(
        context: Context,
        request: FillRequest,
        callback: FillCallback,
        cancellationSignal: CancellationSignal,
        autofillServiceManager: AutofillServiceManager,
        telemetryManager: TelemetryManager,
        accountManager: AccountManager
    ) {
        val windowNode = getWindowNodes(request.fillContexts).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            PassLogger.i(TAG, "No window node found")
            callback.onSuccess(null)
            return
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.e(TAG, exception)
            callback.onSuccess(null)
        }
        val job = CoroutineScope(Dispatchers.IO)
            .launch(handler) {
                searchAndFill(
                    context = context,
                    windowNode = windowNode,
                    callback = callback,
                    request = request,
                    autofillServiceManager = autofillServiceManager,
                    telemetryManager = telemetryManager,
                    accountManager = accountManager
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
        telemetryManager: TelemetryManager,
        accountManager: AccountManager
    ) {
        val shouldAutofill = shouldAutofill(
            accountManager = accountManager,
            request = request,
            windowNode = windowNode,
            autofillServiceManager = autofillServiceManager
        )
        val assistInfo = when (shouldAutofill) {
            is ShouldAutofillResult.No -> {
                PassLogger.i(TAG, "Should not autofill")
                callback.onSuccess(null)
                return
            }
            is ShouldAutofillResult.Yes -> {
                PassLogger.i(TAG, "Should autofill")
                shouldAutofill.assistInfo
            }
        }

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
            request.inlineSuggestionsRequest?.let {
                autofillServiceManager.createSuggestedItemsDatasetList(
                    autofillData = autofillData,
                    inlineSuggestionsRequest = it
                )
            } ?: emptyList()
        } else {
            autofillServiceManager.createMenuPresentationDataset(autofillData)
        }
        datasetList.forEach {
            responseBuilder.addDataset(it)
        }

        val isBrowser = packageNameOption.map { BROWSERS.contains(it) }.value() ?: false
        responseBuilder.addSaveInfo(
            cluster = assistInfo.cluster,
            currentClientState = request.clientState ?: Bundle(),
            isBrowser = isBrowser,
            autofillSessionId = request.id
        )
        return if (!currentCoroutineContext().isActive) {
            PassLogger.i(TAG, "Job was cancelled")
            callback.onSuccess(null)
        } else {
            telemetryManager.sendEvent(AutofillDisplayed(AutofillTriggerSource.Source))
            callback.onSuccess(responseBuilder.build())
        }
    }

    @Suppress("ReturnCount")
    private suspend fun shouldAutofill(
        accountManager: AccountManager,
        request: FillRequest,
        windowNode: AssistStructure.WindowNode,
        autofillServiceManager: AutofillServiceManager
    ): ShouldAutofillResult {
        val currentUser = accountManager.getPrimaryUserId().first()
        if (currentUser == null) {
            PassLogger.d(TAG, "No user found")
            return ShouldAutofillResult.No
        }
        val requestFlags: List<RequestFlags> = RequestFlags.fromValue(request.flags)
        val extractionResult = NodeExtractor(requestFlags).extract(windowNode.rootViewNode)
        if (extractionResult.fields.isEmpty()) {
            PassLogger.d(TAG, "No fields found")
            return ShouldAutofillResult.No
        }
        PassLogger.d(TAG, "Fields found: ${extractionResult.fields.map { it.type }.joinToString()}")

        val clusteredNodes = NodeClusterer.cluster(extractionResult.fields)
        PassLogger.d(TAG, "Clusters found: ${clusteredNodes.joinToString()}")

        val focusedCluster = clusteredNodes.focused()

        if (focusedCluster == NodeCluster.Empty) {
            PassLogger.d(TAG, "No focused cluster found")
            return ShouldAutofillResult.No
        }

        if (focusedCluster is NodeCluster.CreditCard) {
            if (!autofillServiceManager.isCreditCardAutofillEnabled()) {
                PassLogger.d(TAG, "Credit card autofill disabled")
                return ShouldAutofillResult.No
            }
        }

        val assistInfo = AssistInfo(
            cluster = focusedCluster,
            url = extractionResult.url
        )

        return ShouldAutofillResult.Yes(assistInfo)
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

    sealed interface ShouldAutofillResult {
        object No : ShouldAutofillResult
        data class Yes(val assistInfo: AssistInfo) : ShouldAutofillResult
    }
}
