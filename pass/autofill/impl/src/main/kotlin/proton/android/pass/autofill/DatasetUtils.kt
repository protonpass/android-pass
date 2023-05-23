package proton.android.pass.autofill

import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.Presentations
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.asAndroid
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some

object DatasetUtils {

    internal fun buildDataset(
        context: Context,
        dsbOptions: DatasetBuilderOptions,
        autofillMappings: Option<AutofillMappings> = None,
        assistFields: List<AssistField>
    ): Dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        buildDatasetGTE33(
            autofillMappings = autofillMappings,
            dsbOptions = dsbOptions,
            assistFields = assistFields
        )
    } else {
        buildDatasetLT33(
            context = context,
            autofillMappings = autofillMappings,
            dsbOptions = dsbOptions,
            assistFields = assistFields
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildDatasetGTE33(
        dsbOptions: DatasetBuilderOptions,
        autofillMappings: Option<AutofillMappings>,
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
        if (autofillMappings is Some) {
            autofillMappings.value.mappings
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

    @Suppress("DEPRECATION")
    private fun buildDatasetLT33(
        context: Context,
        dsbOptions: DatasetBuilderOptions,
        autofillMappings: Option<AutofillMappings>,
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

        if (autofillMappings is Some) {
            autofillMappings.value.mappings
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
