package proton.android.pass.autofill.entities

data class DatasetMapping(
    val autofillFieldId: AutofillFieldId,
    val contents: String,
    val displayValue: String
)
