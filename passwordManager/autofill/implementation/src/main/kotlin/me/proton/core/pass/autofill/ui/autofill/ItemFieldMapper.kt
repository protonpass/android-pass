package me.proton.core.pass.autofill.ui.autofill

import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.autofill.entities.AutofillResponse
import me.proton.core.pass.autofill.entities.DatasetMapping
import me.proton.core.pass.autofill.entities.FieldType
import me.proton.core.pass.autofill.entities.SearchCredentialsInfo

object ItemFieldMapper {
    fun mapFields(item: AutofillItem, info: SearchCredentialsInfo): AutofillResponse {
        val mappings = when (item) {
            is AutofillItem.Login -> getMappingsForLoginItem(item, info)
            is AutofillItem.Unknown -> emptyList()
        }
        return AutofillResponse(mappings)
    }

    private fun getMappingsForLoginItem(item: AutofillItem.Login, info: SearchCredentialsInfo): List<DatasetMapping> {
        val mappingList = mutableListOf<DatasetMapping>()
        var loginField = info.assistFields.firstOrNull { it.type == FieldType.Email }
        if (loginField == null) {
            loginField = info.assistFields.firstOrNull { it.type == FieldType.Username }
        }
        if (loginField != null) {
            mappingList.add(DatasetMapping(loginField.id, item.username, item.username))
        }

        val passwordField = info.assistFields.firstOrNull { it.type == FieldType.Password }
        if (passwordField != null) {
            mappingList.add(DatasetMapping(passwordField.id, item.password, ""))
        }

        return mappingList
    }
}
