package me.proton.pass.autofill

import android.app.PendingIntent
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import me.proton.android.pass.log.PassLogger
import me.proton.pass.autofill.entities.AssistInfo
import me.proton.pass.autofill.entities.AutofillResponse
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some

object DatasetUtils {

    internal fun generateDataset(packageName: String, response: AutofillResponse): Dataset {

        val datasetBuilder = Dataset.Builder()
        response.mappings.forEach { mapping ->
            val remoteView = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            remoteView.setTextViewText(android.R.id.text1, mapping.displayValue)
            PassLogger.d(
                "VicLog",
                "autofillId: " + mapping.autofillFieldId.asAndroid().autofillId.toString()
            )
            PassLogger.d(
                "VicLog",
                "contents: " + AutofillValue.forText(mapping.contents).toString()
            )
            PassLogger.d("VicLog", "displayValue: " + mapping.displayValue)
            datasetBuilder.setValue(
                mapping.autofillFieldId.asAndroid().autofillId,
                AutofillValue.forText(mapping.contents),
                remoteView
            )
        }
        return datasetBuilder.build()
    }

    internal fun buildDataset(
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistInfo: AssistInfo
    ): Dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        buildDatasetGTE33(
            authenticateView = authenticateView,
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent,
            assistInfo = assistInfo
        )
    } else {
        buildDatasetLT33(
            authenticateView = authenticateView,
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent,
            assistInfo = assistInfo
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildDatasetGTE33(
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistInfo: AssistInfo
    ): Dataset {
        val presentationsBuilder = Presentations.Builder()
        if (authenticateView is Some) {
            presentationsBuilder.setMenuPresentation(authenticateView.value)
        }
        if (inlinePresentation is Some) {
            presentationsBuilder.setInlinePresentation(inlinePresentation.value)
        }
        val datasetBuilder = Dataset.Builder(presentationsBuilder.build())
        if (pendingIntent is Some) {
            datasetBuilder.setAuthentication(pendingIntent.value.intentSender)
        }
        for (value in assistInfo.fields) {
            datasetBuilder.setField(value.id.asAndroid().autofillId, null)
        }
        return datasetBuilder.build()
    }

    private fun buildDatasetLT33(
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistInfo: AssistInfo
    ): Dataset {
        val datasetBuilder = if (authenticateView is Some) {
            Dataset.Builder(authenticateView.value)
        } else {
            Dataset.Builder()
        }
        if (inlinePresentation is Some && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setInlinePresentation(inlinePresentation.value)
        }
        if (pendingIntent is Some) {
            datasetBuilder.setAuthentication(pendingIntent.value.intentSender)
        }
        for (value in assistInfo.fields) {
            datasetBuilder.setValue(
                value.id.asAndroid().autofillId,
                null
            )
        }
        return datasetBuilder.build()
    }
}
