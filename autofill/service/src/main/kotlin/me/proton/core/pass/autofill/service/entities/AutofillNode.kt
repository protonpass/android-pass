package me.proton.core.pass.autofill.service.entities

import android.view.autofill.AutofillValue

class AutofillNode(
    val id: AutofillFieldId?,
    val className: String?,
    val isImportantForAutofill: Boolean,
    val text: String?,
    val autofillValue: AutofillValue?,
    val inputType: Int,
    val autofillHints: List<String>,
    val htmlAttributes: List<Pair<String, String>>,
    val children: List<AutofillNode>
)
