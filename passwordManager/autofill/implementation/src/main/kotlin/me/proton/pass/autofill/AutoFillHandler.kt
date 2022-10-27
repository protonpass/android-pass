package me.proton.pass.autofill

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.service.autofill.SaveInfo
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.pass.autofill.Utils.getWindowNodes
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AssistInfo
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.autofill.AutofillActivity
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.toOption
import kotlin.coroutines.coroutineContext

object AutoFillHandler {

    private const val TAG = "AutoFillHandler"

    fun handleAutofill(
        context: Context,
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            callback.onFailure(context.getString(R.string.error_cant_find_matching_fields))
            return
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            PassLogger.e(TAG, exception)
        }
        val job = CoroutineScope(Dispatchers.IO).launch(handler) {
            searchAndFill(context, windowNode, callback, request)
        }

        cancellationSignal.setOnCancelListener {
            job.cancel()
        }
    }

    @Suppress("UnusedPrivateMember")
    private suspend fun searchAndFill(
        context: Context,
        windowNode: AssistStructure.WindowNode,
        callback: FillCallback,
        request: FillRequest
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)
        if (assistInfo.fields.isEmpty()) return

        if (!coroutineContext.isActive) {
            callback.onFailure(context.getString(R.string.error_credentials_not_found))
            return
        }

        val autofillIds = assistInfo.fields.map {
            (it.id as AndroidAutofillFieldId).autofillId
        }.toTypedArray()
        // Single Dataset to force user authentication
        val dataset = buildDataset(context, windowNode, assistInfo, request)
        val saveInfo = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC, autofillIds).build()
        val response = FillResponse.Builder()
            .addDataset(dataset)
            .setSaveInfo(saveInfo)
            .build()

        callback.onSuccess(response)
    }

    private fun buildDataset(
        context: Context,
        windowNode: AssistStructure.WindowNode,
        assistInfo: AssistInfo,
        request: FillRequest
    ): Dataset {
        val data = AutofillData(
            assistInfo,
            Utils.getApplicationPackageName(windowNode)
        )
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            AutofillActivity.REQUEST_CODE,
            AutofillActivity.newIntent(context, data),
            flag
        )
        val authenticateView = RemoteViews(
            context.packageName,
            android.R.layout.simple_list_item_1
        ).apply {
            setTextViewText(
                android.R.id.text1,
                context.getString(R.string.autofill_authenticate_prompt)
            )
        }
        val inlinePresentation: Option<InlinePresentation> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                buildInlineSuggestion(context, request, pendingIntent).toOption()
            } else {
                None
            }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            buildDatasetGTE33(authenticateView, inlinePresentation, pendingIntent, assistInfo)
        } else {
            buildDatasetLT33(authenticateView, inlinePresentation, pendingIntent, assistInfo)
        }
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildInlinePresentation(
        context: Context,
        inlinePresentationSpec: InlinePresentationSpec,
        pendingIntent: PendingIntent
    ): InlinePresentation? {
        val imeStyle = inlinePresentationSpec.style
        if (!UiVersions.getVersions(imeStyle).contains(UiVersions.INLINE_UI_VERSION_1))
            return null

        return InlinePresentation(
            InlineSuggestionUi.newContentBuilder(pendingIntent)
                .setContentDescription(context.getString(R.string.inline_suggestions_open_app))
                .setTitle(context.getString(R.string.inline_suggestions_open_app))
                .build()
                .slice,
            inlinePresentationSpec,
            false
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildInlineSuggestion(
        context: Context,
        request: FillRequest,
        pendingIntent: PendingIntent
    ): InlinePresentation? = request.inlineSuggestionsRequest
        ?.inlinePresentationSpecs
        .orEmpty()
        .firstNotNullOfOrNull { buildInlinePresentation(context, it, pendingIntent) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildDatasetGTE33(
        authenticateView: RemoteViews,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: PendingIntent,
        assistInfo: AssistInfo
    ): Dataset {
        val presentationsBuilder = Presentations.Builder()
        presentationsBuilder.setMenuPresentation(authenticateView)
        if (inlinePresentation is Some) {
            presentationsBuilder.setInlinePresentation(inlinePresentation.value)
        }
        val datasetBuilder = Dataset.Builder(presentationsBuilder.build())
        datasetBuilder.setAuthentication(pendingIntent.intentSender)
        for (value in assistInfo.fields) {
            datasetBuilder.setField(value.id.asAndroid().autofillId, null)
        }
        return datasetBuilder.build()
    }

    private fun buildDatasetLT33(
        authenticateView: RemoteViews,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: PendingIntent,
        assistInfo: AssistInfo
    ): Dataset {
        val datasetBuilder = Dataset.Builder(authenticateView)
        if (inlinePresentation is Some && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setInlinePresentation(inlinePresentation.value)
        }
        datasetBuilder.setAuthentication(pendingIntent.intentSender)
        for (value in assistInfo.fields) {
            datasetBuilder.setValue(value.id.asAndroid().autofillId, null)
        }
        datasetBuilder.build()
    }
}
