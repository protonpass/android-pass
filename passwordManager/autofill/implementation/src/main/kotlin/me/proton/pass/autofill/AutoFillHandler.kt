package me.proton.pass.autofill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.Presentations
import android.service.autofill.SaveInfo
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.pass.autofill.Utils.getWindowNodes
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.autofill.AutofillActivity
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
            searchAndFill(context, windowNode, callback)
        }

        cancellationSignal.setOnCancelListener {
            job.cancel()
        }
    }

    @Suppress("UnusedPrivateMember")
    private suspend fun searchAndFill(
        context: Context,
        windowNode: AssistStructure.WindowNode,
        callback: FillCallback
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)
        val assistFields: List<AssistField> = assistInfo.fields

        if (assistFields.isEmpty()) return

        if (!coroutineContext.isActive) {
            callback.onFailure(context.getString(R.string.error_credentials_not_found))
            return
        }

        val autofillIds = assistFields.map {
            (it.id as AndroidAutofillFieldId).autofillId
        }.toTypedArray()
        // Single Dataset to force user authentication
        val dataset = buildDataset(context, windowNode, assistFields)
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
        assistFields: List<AssistField>
    ): Dataset {
        val data = AutofillData(
            assistFields,
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val presentations = Presentations.Builder()
                .setMenuPresentation(authenticateView)
                .build()
            Dataset.Builder(presentations)
                .apply {
                    setAuthentication(pendingIntent.intentSender)
                    for (value in assistFields) {
                        setField(value.id.asAndroid().autofillId, null)
                    }
                }
                .build()
        } else {
            Dataset.Builder(authenticateView)
                .apply {
                    setAuthentication(pendingIntent.intentSender)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    val inlineSuggestionSpecs = request.inlineSuggestionsRequest
//                        ?.inlinePresentationSpecs.orEmpty()
//                    for (spec in inlineSuggestionSpecs) {
//                        addInlineSuggestion(this, spec, pendingIntent)
//                    }
//                }
                    for (value in assistFields) {
                        setValue(value.id.asAndroid().autofillId, null)
                    }
                }
                .build()
        }
    }
}
