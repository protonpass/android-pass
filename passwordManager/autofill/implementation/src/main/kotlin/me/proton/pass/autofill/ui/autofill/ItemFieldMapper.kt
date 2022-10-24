package me.proton.pass.autofill.ui.autofill

import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AutofillItem
import me.proton.pass.autofill.entities.AutofillResponse
import me.proton.pass.autofill.entities.DatasetMapping
import me.proton.pass.autofill.entities.FieldType

object ItemFieldMapper {
    fun mapFields(
        item: AutofillItem,
        androidAutofillFieldIds: List<AndroidAutofillFieldId>,
        autofillTypes: List<FieldType>
    ): AutofillResponse {
        val mappings = when (item) {
            is AutofillItem.Login -> getMappingsForLoginItem(
                item,
                androidAutofillFieldIds,
                autofillTypes
            )
            is AutofillItem.Unknown -> emptyList()
        }
        return AutofillResponse(mappings)
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
