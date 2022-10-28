package me.proton.pass.autofill.extensions

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import me.proton.pass.autofill.DatasetUtils
import me.proton.pass.autofill.InlinePresentationUtils
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AssistInfo
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.service.R
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.some
import me.proton.pass.common.api.toOption

@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addInlineSuggestion(
    inlinePresentation: InlinePresentation,
    assistInfo: AssistInfo,
    pendingIntent: Option<PendingIntent>
) {
    val dataset = DatasetUtils.buildDataset(
        authenticateView = None,
        inlinePresentation = inlinePresentation.some(),
        pendingIntent = pendingIntent,
        assistInfo = assistInfo
    )
    addDataset(dataset)
}

@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addOpenAppInlineSuggestion(
    context: Context,
    autofillData: AutofillData,
    inlinePresentationSpec: InlinePresentationSpec,
    pendingIntent: PendingIntent
) {
    val defaultTitle = context.getString(R.string.inline_suggestions_open_app)
    val inlinePresentation: InlinePresentation =
        InlinePresentationUtils.create(
            title = defaultTitle,
            subtitle = None,
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = pendingIntent
        )
    addInlineSuggestion(
        inlinePresentation = inlinePresentation,
        assistInfo = autofillData.assistInfo,
        pendingIntent = pendingIntent.toOption()
    )
}

internal fun FillResponse.Builder.addSaveInfo(
    assistInfo: AssistInfo
) {
    val autofillIds: Array<AutofillId> = assistInfo.fields
        .map { (it.id as AndroidAutofillFieldId).autofillId }
        .toTypedArray()
    val saveInfo = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC, autofillIds).build()
    setSaveInfo(saveInfo)
}
