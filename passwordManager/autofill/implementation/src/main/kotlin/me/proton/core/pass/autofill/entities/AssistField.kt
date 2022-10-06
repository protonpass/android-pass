package me.proton.core.pass.autofill.entities

import android.view.autofill.AutofillValue

data class AssistField(
    val id: AutofillFieldId,
    val type: FieldType?,
    val value: AutofillValue?,
    val text: String?
)
