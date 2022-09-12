package me.proton.core.pass.autofill.service.entities

data class DatasetMapping(
    val autofillFieldId: AutofillFieldId,
    val contents: String,
    val displayValue: String
)
