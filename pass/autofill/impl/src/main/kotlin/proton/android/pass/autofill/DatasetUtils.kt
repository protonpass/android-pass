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
        options: DatasetBuilderOptions,
        autofillMappings: Option<AutofillMappings> = None,
        assistFields: List<AssistField> = emptyList()
    ): Dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        buildDatasetGTE33(
            autofillMappings = autofillMappings,
            dsbOptions = options,
            assistFields = assistFields
        )
    } else {
        buildDatasetLT33(
            autofillMappings = autofillMappings,
            dsbOptions = options,
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
        if (dsbOptions.remoteViewPresentation is Some) {
            presentationsBuilder.setMenuPresentation(dsbOptions.remoteViewPresentation.value)
        }
        if (dsbOptions.inlinePresentation is Some) {
            presentationsBuilder.setInlinePresentation(dsbOptions.inlinePresentation.value)
        }
        val datasetBuilder = Dataset.Builder(presentationsBuilder.build())
        if (dsbOptions.pendingIntent is Some) {
            datasetBuilder.setAuthentication(dsbOptions.pendingIntent.value.intentSender)
        }
        if (autofillMappings is Some) {
            datasetBuilder.fillWithMappings(autofillMappings.value)
        } else {
            datasetBuilder.createDataHolders(assistFields)
        }
        return datasetBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Dataset.Builder.fillWithMappings(autofillMappings: AutofillMappings): Dataset.Builder {
        autofillMappings.mappings
            .forEach { mapping ->
                val fieldBuilder = Field.Builder()
                fieldBuilder.setValue(AutofillValue.forText(mapping.contents))
                setField(
                    mapping.autofillFieldId.asAndroid().autofillId,
                    fieldBuilder.build()
                )
            }
        return this
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Dataset.Builder.createDataHolders(assistFields: List<AssistField>): Dataset.Builder {
        for (field in assistFields) {
            setField(field.id.asAndroid().autofillId, Field.Builder().build())
        }
        return this
    }

    @Suppress("DEPRECATION")
    private fun buildDatasetLT33(
        dsbOptions: DatasetBuilderOptions,
        autofillMappings: Option<AutofillMappings>,
        assistFields: List<AssistField>
    ): Dataset {
        val datasetBuilder = if (dsbOptions.remoteViewPresentation is Some) {
            Dataset.Builder(dsbOptions.remoteViewPresentation.value)
        } else {
            Dataset.Builder()
        }

        if (dsbOptions.inlinePresentation is Some && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setInlinePresentation(dsbOptions.inlinePresentation.value)
        }

        if (dsbOptions.pendingIntent is Some) {
            datasetBuilder.setAuthentication(dsbOptions.pendingIntent.value.intentSender)
        }

        if (dsbOptions.remoteViewPresentation is Some) {
            if (autofillMappings is Some) {
                datasetBuilder.fillWithMappings(
                    autofillMappings = autofillMappings.value,
                    remoteView = dsbOptions.remoteViewPresentation.value
                )
            } else {
                datasetBuilder.createDataHolders(
                    assistFields = assistFields,
                    remoteView = dsbOptions.remoteViewPresentation.value
                )
            }
        }

        return datasetBuilder.build()
    }

    @Suppress("DEPRECATION")
    private fun Dataset.Builder.createDataHolders(
        assistFields: List<AssistField>,
        remoteView: RemoteViews,
    ): Dataset.Builder {
        for (value in assistFields) {
            setValue(value.id.asAndroid().autofillId, null, remoteView)
        }
        return this
    }

    @Suppress("DEPRECATION")
    private fun Dataset.Builder.fillWithMappings(
        autofillMappings: AutofillMappings,
        remoteView: RemoteViews,
    ): Dataset.Builder {
        autofillMappings.mappings
            .forEach { mapping ->
                setValue(
                    mapping.autofillFieldId.asAndroid().autofillId,
                    AutofillValue.forText(mapping.contents),
                    remoteView
                )
            }
        return this
    }
}
