package me.proton.core.pass.autofill.entities

data class AutofillData(
    val assistFields: List<AssistField>,
    val packageName: String
)
