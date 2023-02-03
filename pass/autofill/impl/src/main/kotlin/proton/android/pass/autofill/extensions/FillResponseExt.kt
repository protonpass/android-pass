package proton.android.pass.autofill.extensions

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import me.proton.pass.autofill.service.R
import proton.android.pass.autofill.DatasetBuilderOptions
import proton.android.pass.autofill.DatasetUtils
import proton.android.pass.autofill.InlinePresentationUtils
import proton.android.pass.autofill.PendingIntentUtils
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.itemName
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.api.extensions.loginUsername
import proton.pass.domain.Item

@Suppress("LongParameterList")
@RequiresApi(Build.VERSION_CODES.R)
private fun FillResponse.Builder.addInlineSuggestion(
    context: Context,
    inlinePresentation: Option<InlinePresentation>,
    pendingIntent: Option<PendingIntent>,
    assistFields: List<AssistField>
) {
    val dataset = DatasetUtils.buildDataset(
        context = context,
        autofillMappings = None,
        dsbOptions = DatasetBuilderOptions(
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent
        ),
        assistFields = assistFields
    )
    addDataset(dataset)
}

@Suppress("LongParameterList")
@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addItemInlineSuggestion(
    context: Context,
    encryptionContext: EncryptionContext,
    itemOption: Option<Item>,
    inlinePresentationSpec: InlinePresentationSpec,
    assistFields: List<AssistField>,
    pendingIntent: PendingIntent
) {
    val inlinePresentation = itemOption
        .map { item ->
            InlinePresentationUtils.create(
                title = item.itemName(encryptionContext),
                subtitle = item.loginUsername(),
                inlinePresentationSpec = inlinePresentationSpec,
                pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context)
            )
        }

    addInlineSuggestion(
        context = context,
        inlinePresentation = inlinePresentation,
        pendingIntent = pendingIntent.toOption(),
        assistFields = assistFields
    )
}

@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addOpenAppInlineSuggestion(
    context: Context,
    inlinePresentationSpec: InlinePresentationSpec,
    pendingIntent: PendingIntent,
    assistFields: List<AssistField>
) {
    val defaultTitle = context.getString(R.string.inline_suggestions_open_app)
    val inlinePresentation: InlinePresentation =
        InlinePresentationUtils.create(
            title = defaultTitle,
            subtitle = None,
            inlinePresentationSpec = inlinePresentationSpec,
            pendingIntent = PendingIntentUtils.getLongPressInlinePendingIntent(context)
        )
    addInlineSuggestion(
        context = context,
        inlinePresentation = inlinePresentation.toOption(),
        pendingIntent = pendingIntent.toOption(),
        assistFields = assistFields
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
