package me.proton.pass.autofill

import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.Presentations
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.autofill.extensions.toAutofillItem
import me.proton.pass.autofill.ui.autofill.ItemFieldMapper
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.domain.Item

object DatasetUtils {

    internal fun buildDataset(
        context: Context,
        cryptoContext: CryptoContext,
        dsbOptions: DatasetBuilderOptions,
        item: Option<Item>,
        assistFields: List<AssistField>
    ): Dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        buildDatasetGTE33(
            cryptoContext = cryptoContext,
            item = item,
            dsbOptions = dsbOptions,
            assistFields = assistFields
        )
    } else {
        buildDatasetLT33(
            context = context,
            cryptoContext = cryptoContext,
            item = item,
            dsbOptions = dsbOptions,
            assistFields = assistFields
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildDatasetGTE33(
        cryptoContext: CryptoContext,
        dsbOptions: DatasetBuilderOptions,
        item: Option<Item>,
        assistFields: List<AssistField>
    ): Dataset {
        val presentationsBuilder = Presentations.Builder()
        if (dsbOptions.authenticateView is Some) {
            presentationsBuilder.setMenuPresentation(dsbOptions.authenticateView.value)
        }
        if (dsbOptions.inlinePresentation is Some) {
            presentationsBuilder.setInlinePresentation(dsbOptions.inlinePresentation.value)
        }
        val datasetBuilder = Dataset.Builder(presentationsBuilder.build())
        if (dsbOptions.pendingIntent is Some) {
            datasetBuilder.setAuthentication(dsbOptions.pendingIntent.value.intentSender)
        }
        if (item is Some) {
            val autofillItem = item.value.toAutofillItem(cryptoContext.keyStoreCrypto)
            val autofillMappings = ItemFieldMapper.mapFields(
                autofillItem,
                assistFields.map { it.id.asAndroid() },
                assistFields.map { it.type ?: FieldType.Unknown }
            )
            autofillMappings.mappings
                .forEach { mapping ->
                    val fieldBuilder = Field.Builder()
                    fieldBuilder.setValue(AutofillValue.forText(mapping.contents))
                    datasetBuilder.setField(
                        mapping.autofillFieldId.asAndroid().autofillId,
                        fieldBuilder.build()
                    )
                }
        } else {
            for (field in assistFields) {
                datasetBuilder.setField(field.id.asAndroid().autofillId, Field.Builder().build())
            }
        }
        return datasetBuilder.build()
    }

    private fun buildDatasetLT33(
        context: Context,
        cryptoContext: CryptoContext,
        dsbOptions: DatasetBuilderOptions,
        item: Option<Item>,
        assistFields: List<AssistField>
    ): Dataset {
        val datasetBuilder = if (dsbOptions.authenticateView is Some) {
            Dataset.Builder(dsbOptions.authenticateView.value)
        } else {
            Dataset.Builder()
        }

        if (dsbOptions.inlinePresentation is Some && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setInlinePresentation(dsbOptions.inlinePresentation.value)
        }

        if (dsbOptions.pendingIntent is Some) {
            datasetBuilder.setAuthentication(dsbOptions.pendingIntent.value.intentSender)
        }

        if (item is Some) {
            val autofillItem = item.value.toAutofillItem(cryptoContext.keyStoreCrypto)
            val autofillMappings = ItemFieldMapper.mapFields(
                autofillItem,
                assistFields.map { it.id.asAndroid() },
                assistFields.map { it.type ?: FieldType.Unknown }
            )
            autofillMappings.mappings
                .forEach { mapping ->
                    datasetBuilder.setValue(
                        mapping.autofillFieldId.asAndroid().autofillId,
                        AutofillValue.forText(mapping.contents),
                        createView(context, mapping.displayValue)
                    )
                }
        } else {
            for (value in assistFields) {
                datasetBuilder.setValue(value.id.asAndroid().autofillId, null)
            }
        }

        return datasetBuilder.build()
    }

    private fun createView(context: Context, value: String): RemoteViews {
        val remoteView = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        remoteView.setTextViewText(android.R.id.text1, value)
        return remoteView
    }
}
