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
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.autofill.DatasetBuilderOptions
import me.proton.pass.autofill.DatasetUtils
import me.proton.pass.autofill.InlinePresentationUtils
import me.proton.pass.autofill.PendingIntentUtils
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.AssistInfo
import me.proton.pass.autofill.entities.AutofillMappings
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.autofill.ItemFieldMapper
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.some
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.Item

@Suppress("LongParameterList")
@RequiresApi(Build.VERSION_CODES.R)
private fun FillResponse.Builder.addInlineSuggestion(
    context: Context,
    cryptoContext: CryptoContext,
    itemOption: Option<Item>,
    inlinePresentation: Option<InlinePresentation>,
    pendingIntent: Option<PendingIntent>,
    assistFields: List<AssistField>
) {
    val autofillMappings: Option<AutofillMappings> = itemOption.map { item ->
        ItemFieldMapper.mapFields(
            item.toAutofillItem(cryptoContext.keyStoreCrypto),
            assistFields.map { it.id.asAndroid() },
            assistFields.map { it.type ?: FieldType.Unknown }
        )
    }

    val dataset = DatasetUtils.buildDataset(
        context = context,
        autofillMappings = autofillMappings,
        dsbOptions = DatasetBuilderOptions(
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent
        ),
        assistFields = assistFields
    )
    addDataset(dataset)
}

@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addItemInlineSuggestion(
    context: Context,
    cryptoContext: CryptoContext,
    item: Option<Item>,
    inlinePresentationSpec: InlinePresentationSpec,
    assistFields: List<AssistField>
) {
    val inlinePresentation = when (item) {
        None -> None
        is Some -> {
            InlinePresentationUtils.create(
                title = item.value.title.decrypt(cryptoContext.keyStoreCrypto),
                subtitle = Some("subtitle"),
                inlinePresentationSpec = inlinePresentationSpec,
                pendingIntent = PendingIntentUtils.getEmptyPendingIntent(context)
            ).some()
        }
    }

    addInlineSuggestion(
        context = context,
        cryptoContext = cryptoContext,
        itemOption = item,
        inlinePresentation = inlinePresentation,
        pendingIntent = None,
        assistFields = assistFields
    )
}

@RequiresApi(Build.VERSION_CODES.R)
internal fun FillResponse.Builder.addOpenAppInlineSuggestion(
    context: Context,
    cryptoContext: CryptoContext,
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
            pendingIntent = pendingIntent
        )
    addInlineSuggestion(
        context = context,
        cryptoContext = cryptoContext,
        itemOption = None,
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
