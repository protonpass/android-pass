package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.entities.FieldType

object ItemFieldMapper {
    fun mapFields(
        item: AutofillItem,
        androidAutofillFieldIds: List<AndroidAutofillFieldId>,
        autofillTypes: List<FieldType>
    ): AutofillMappings {
        val mappings = when (item) {
            is AutofillItem.Login -> getMappingsForLoginItem(
                item,
                androidAutofillFieldIds,
                autofillTypes
            )
            is AutofillItem.Unknown -> emptyList()
        }
        return AutofillMappings(mappings)
    }

    private fun getMappingsForLoginItem(
        item: AutofillItem.Login,
        androidAutofillFieldIds: List<AndroidAutofillFieldId>,
        autofillTypes: List<FieldType>
    ): List<DatasetMapping> {
        val mappingList = mutableListOf<DatasetMapping>()
        var loginIndex = autofillTypes.indexOfFirst { it == FieldType.Email }
        if (loginIndex == -1) {
            loginIndex = autofillTypes.indexOfFirst { it == FieldType.Username }
        }
        if (loginIndex != -1) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[loginIndex],
                    contents = item.username,
                    displayValue = item.username
                )
            )
        }

        val passwordIndex = autofillTypes.indexOfFirst { it == FieldType.Password }
        if (passwordIndex != -1) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[passwordIndex],
                    contents = item.password,
                    displayValue = ""
                )
            )
        }

        return mappingList
    }
}
