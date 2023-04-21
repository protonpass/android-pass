package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger

object ItemFieldMapper {

    private const val TAG = "ItemFieldMapper"

    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        androidAutofillFieldIds: List<AndroidAutofillFieldId>,
        autofillTypes: List<FieldType>
    ): AutofillMappings {
        val mappingList = mutableListOf<DatasetMapping>()
        var loginIndex = autofillTypes.indexOfFirst { it == FieldType.Email }
        if (loginIndex == -1) {
            loginIndex = autofillTypes.indexOfFirst { it == FieldType.Username }
        }
        if (loginIndex != -1) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[loginIndex],
                    contents = autofillItem.username,
                    displayValue = autofillItem.username
                )
            )
        }
        val passwordIndex = autofillTypes.indexOfFirst { it == FieldType.Password }
        if (passwordIndex != -1) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[passwordIndex],
                    contents = encryptionContext.decrypt(autofillItem.password),
                    displayValue = ""
                )
            )
        }

        if (mappingList.isEmpty()) {
            val message = "No mappings found for autofill. Detected field types: $autofillTypes"
            PassLogger.e(TAG, IllegalStateException(message), message)
        }

        return AutofillMappings(mappingList)
    }
}
