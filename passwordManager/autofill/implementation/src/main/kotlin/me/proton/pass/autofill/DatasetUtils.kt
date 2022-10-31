package me.proton.pass.autofill

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.AutofillResponse
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.data.extensions.fromParsed
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import proton_pass_item_v1.ItemV1

object DatasetUtils {

    internal fun generateDataset(packageName: String, response: AutofillResponse): Dataset {

        val datasetBuilder = Dataset.Builder()
        response.mappings.forEach { mapping ->
            val remoteView = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            remoteView.setTextViewText(android.R.id.text1, mapping.displayValue)
            datasetBuilder.setValue(
                mapping.autofillFieldId.asAndroid().autofillId,
                AutofillValue.forText(mapping.contents),
                remoteView
            )
        }
        return datasetBuilder.build()
    }

    internal fun buildDataset(
        context: Context,
        cryptoContext: CryptoContext,
        item: Option<Item>,
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistFields: List<AssistField>
    ): Dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        buildDatasetGTE33(
            cryptoContext = cryptoContext,
            item = item,
            authenticateView = authenticateView,
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent,
            assistFields = assistFields
        )
    } else {
        buildDatasetLT33(
            context = context,
            cryptoContext = cryptoContext,
            item = item,
            authenticateView = authenticateView,
            inlinePresentation = inlinePresentation,
            pendingIntent = pendingIntent,
            assistFields = assistFields
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildDatasetGTE33(
        cryptoContext: CryptoContext,
        item: Option<Item>,
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistFields: List<AssistField>
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
        if (item is Some) {
            val itemContents = item.value.content.decrypt(cryptoContext.keyStoreCrypto)
            val itemProto = ItemV1.Item.parseFrom(itemContents.array)
            val itemType: ItemType = ItemType.fromParsed(cryptoContext, itemProto)
            if (itemType is ItemType.Login) {
                for (field in assistFields) {
                    val content = when (field.type) {
                        FieldType.Username -> itemType.username
                        FieldType.Email -> itemType.username
                        FieldType.Password -> itemType.password.decrypt(cryptoContext.keyStoreCrypto)
                        else -> ""
                    }

                    val fieldBuilder = Field.Builder()
                    fieldBuilder.setValue(AutofillValue.forText(content))
                    datasetBuilder.setField(field.id.asAndroid().autofillId, fieldBuilder.build())
                }
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
        item: Option<Item>,
        authenticateView: Option<RemoteViews>,
        inlinePresentation: Option<InlinePresentation>,
        pendingIntent: Option<PendingIntent>,
        assistFields: List<AssistField>
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

        if (item is Some) {
            val itemContents = item.value.content.decrypt(cryptoContext.keyStoreCrypto)
            val itemProto = ItemV1.Item.parseFrom(itemContents.array)
            val itemType: ItemType = ItemType.fromParsed(cryptoContext, itemProto)
            if (itemType is ItemType.Login) {
                for (value in assistFields) {
                    val content = when (value.type) {
                        FieldType.Username -> itemType.username
                        FieldType.Email -> itemType.username
                        FieldType.Password -> itemType.password.decrypt(cryptoContext.keyStoreCrypto)
                        else -> ""
                    }
                    datasetBuilder.setValue(
                        value.id.asAndroid().autofillId,
                        AutofillValue.forText(content),
                        createView(context, content)
                    )
                }
            }
        } else {
            for (value in assistFields) {
                value.type
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
